package org.muoncore.extension.amqp;

import com.rabbitmq.client.*;
import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class AMQPEventTransport implements MuonResourceTransport,MuonBroadcastTransport {

    private Logger log = Logger.getLogger(AMQPEventTransport.class.getName());

    private Connection connection;
    private Channel channel;

    private ExecutorService spinner;

    private String serviceName;

    static String EXCHANGE_NAME ="muon-broadcast";
    static String EXCHANGE_RES ="muon-resource";
    static String RABBIT_HOST = "localhost";

    Map<String, String> maps = new HashMap<String, String>();

    public AMQPEventTransport(String serviceName) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException {
        this.serviceName = serviceName;
        spinner = Executors.newCachedThreadPool();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqp://localhost:5672");
        //factory.setUri("amqp://userName:password@$RABBIT_HOST/");
        connection = factory.newConnection();

        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        channel.exchangeDeclare(EXCHANGE_RES, "topic");
    }

    @Override
    public MuonService.MuonResult emit(String eventName, MuonBroadcastEvent event) {
        //TODO, marshalling.
        String payload = event.getPayload().toString();
        byte[] messageBytes = payload.getBytes();

        MuonService.MuonResult ret = new MuonService.MuonResult();

        //TODO, send the headers... ?
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().headers((Map) event.getHeaders()).build();
        try {
            channel.basicPublish(EXCHANGE_NAME, eventName, props, messageBytes);

            ret.setSuccess(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
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

            channel.basicPublish(EXCHANGE_RES, key, props, messageBytes);

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

    @Override
    public void listenOnResource(final String resource, final String verb, final Muon.EventResourceTransportListener listener) {
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                //TODO, add ability to filter on the verb, service name, onGet ... (probably add it to the routing key)
                try {

                    Map<String, Object> args = new HashMap<String, Object>();
                    args.put("x-message-ttl", 2000);
                    AMQP.Queue.DeclareOk ok = channel.queueDeclare();
                    channel.queueBind(ok.getQueue(), EXCHANGE_RES, serviceName + "." + resource + "." + verb);

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

    @Override
    public void listenOnEvent(final String resource, final Muon.EventBroadcastTransportListener listener) {
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String queueName = null;
                    queueName = channel.queueDeclare().getQueue();

                    channel.queueBind(queueName, EXCHANGE_NAME, resource);

                    log.fine("AMQPChannel : Waiting for messages " + resource);

                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(queueName, true, consumer);

                    while (true) {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        String message = new String(delivery.getBody());

                        log.finer("AMQP: Received '" + message + "'");

                        MuonBroadcastEventBuilder builder = MuonBroadcastEventBuilder.broadcast(resource)
                                .withMimeType(delivery.getProperties().getContentType())
                                .withContent(message);

                        Map<Object, Object> headers = (Map) delivery.getProperties().getHeaders();

                        if (headers != null) {
                            for (Map.Entry<Object, Object> entry : headers.entrySet()) {
                                builder.withHeader(entry.getKey().toString(), entry.getValue().toString());
                            }
                        }

                        MuonBroadcastEvent ev = builder.build();

                        listener.onEvent(resource, ev);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public List<ServiceDescriptor> discoverServices() {
        return null;
    }

    @Override
    public void shutdown() {
        spinner.shutdown();
        try {
            channel.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        //TODO ....
    }

}
