package io.muoncore.extension.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import io.muoncore.Muon;
import io.muoncore.MuonService;
import io.muoncore.transport.MuonMessageEventBuilder;
import io.muoncore.transport.MuonMessageEvent;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class AmqpBroadcast {

    static String EXCHANGE_NAME ="muon-broadcast";

    private Logger log = Logger.getLogger(AmqpBroadcast.class.getName());

    private Channel channel;
    private ExecutorService spinner;

    public AmqpBroadcast(AmqpConnection connection) throws IOException {
        this.channel = connection.getChannel();
        spinner = Executors.newCachedThreadPool();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
    }

    public MuonService.MuonResult broadcast(String eventName, MuonMessageEvent event) {
        //TODO, marshalling.
        byte[] messageBytes = event.getBinaryEncodedContent();

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
    public void listenOnBroadcastEvent(final String resource, final Muon.EventMessageTransportListener listener) {
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String queueName = null;
                    queueName = channel.queueDeclare().getQueue();

                    channel.queueBind(queueName, EXCHANGE_NAME, resource);

                    log.fine("Waiting for discovery broadcast messages " + resource);

                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(queueName, true, consumer);

                    while (true) {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        byte[] body = delivery.getBody();

                        MuonMessageEventBuilder builder = MuonMessageEventBuilder.named(resource);
//                                .withMimeType(delivery.getProperties().getContentType())

                        Map<Object, Object> headers = (Map) delivery.getProperties().getHeaders();

                        if (headers != null) {
                            for (Map.Entry<Object, Object> entry : headers.entrySet()) {
                                builder.withHeader(entry.getKey().toString(), entry.getValue().toString());
                            }
                        }

                        MuonMessageEvent ev = builder.build();
                        ev.setContentType(delivery.getProperties().getContentType());
                        ev.setEncodedBinaryContent(body);

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

    public void shutdown() {
        spinner.shutdown();
    }
}
