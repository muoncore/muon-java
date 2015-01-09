package org.muoncore.extension.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.Muon;
import org.muoncore.MuonService;
import org.muoncore.transports.MuonResourceEvent;
import org.muoncore.transports.MuonResourceEventBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class AmqpResources {

    static String EXCHANGE_NAME ="muon-resource";

    private Logger log = Logger.getLogger(AMQPEventTransport.class.getName());

    private Channel channel;

    private AmqpResources resources;

    private ExecutorService spinner;
    private String serviceName;

    public AmqpResources(Channel channel, String serviceName) throws IOException {
        this.channel = channel;
        this.serviceName = serviceName;
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        spinner = Executors.newCachedThreadPool();
    }

    public MuonService.MuonResult emitForReturn(String eventName, MuonResourceEvent event) {
        //TODO, customise serialisation strategy ... ?
        String payload = null;

        if (event.getPayload() instanceof String) {
            payload = (String) event.getPayload();
        } else {
            payload = JSON.toString(event.getPayload());
        }
        byte[] messageBytes = payload.getBytes();

        MuonService.MuonResult ret = new MuonService.MuonResult();

        try {

            String callbackQueueName = channel.queueDeclare().getQueue();

            //TODO, this generates a new queue for every single presend of this type. likely to break and is highly inefficient
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .replyTo(callbackQueueName)
                    .build();

            String key = event.getUri().getHost() + "." + event.getUri().getPath() + "." + event.getHeaders().get("verb");

            channel.basicPublish(EXCHANGE_NAME, key, props, messageBytes);

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(callbackQueueName, true, consumer);

            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());

            log.finer("AMQP: Received Resource Reply '" + message + "'");

            String mimeType = delivery.getProperties().getContentType();
            Map<String, Object> head = delivery.getProperties().getHeaders();

            MuonResourceEventBuilder builder = MuonResourceEventBuilder.textMessage(message)
                    .withMimeType(mimeType);

            if (head != null){
                for (Map.Entry<String, Object> entry : head.entrySet()) {
                    builder.withHeader(entry.getKey(), (String) entry.getValue());
                }
            }

            ret.setEvent(builder.build());

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void listenOnResource(final String resource, final String verb, final Muon.EventResourceTransportListener listener) {
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                //TODO, add ability to filter on the verb, service name, onGet ... (probably add it to the routing key)
                try {

                    Map<String, Object> args = new HashMap<String, Object>();
                    args.put("x-message-ttl", 2000);
                    AMQP.Queue.DeclareOk ok = channel.queueDeclare();
                    channel.queueBind(ok.getQueue(), EXCHANGE_NAME, serviceName + "." + resource + "." + verb);

                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(ok.getQueue(), false, consumer);

                    log.fine("Waiting for " + verb + " requests " + resource);

                    while (true) {
                        try {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                            log.finer("Got Request " + resource + " " + verb);

                            BasicProperties props = delivery.getProperties();
                            AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                                    .contentType("text/plain")
                                    .correlationId(props.getCorrelationId())
                                    .build();

                            String message = new String(delivery.getBody());

                            log.finer("AMQPChannel : Received " + message);

                            //todo, transport marshalling
                            MuonResourceEvent ev = MuonResourceEventBuilder.textMessage(message)
                                    .withMimeType(delivery.getProperties().getContentType())
                                    .build();

                            String response = listener.onEvent(resource, ev).toString();
                            log.finer("Sending" + response);

                            if (props.getReplyTo() != null) {
                                channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes());
                            } else {
                                log.fine("Discard message, no routing key in properties " + props.getMessageId());
                            }
                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void shutdown() {
        spinner.shutdown();
    }
}
