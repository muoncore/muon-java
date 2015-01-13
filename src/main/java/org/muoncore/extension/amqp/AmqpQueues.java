package org.muoncore.extension.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.muoncore.Muon;
import org.muoncore.MuonClient;
import org.muoncore.MuonService;
import org.muoncore.transports.MuonMessageEventBuilder;
import org.muoncore.transports.MuonMessageEvent;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class AmqpQueues {

    private Logger log = Logger.getLogger(AmqpQueues.class.getName());

    private Channel channel;
    private ExecutorService spinner;

    public AmqpQueues(Channel channel) throws IOException {
        this.channel = channel;
        spinner = Executors.newCachedThreadPool();
    }

    public MuonClient.MuonResult send(String queueName, MuonMessageEvent event) {

        String payload = event.getPayload().toString();
        byte[] messageBytes = payload.getBytes();

        MuonService.MuonResult ret = new MuonService.MuonResult();

        //TODO, send the headers... ?
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().headers((Map) event.getHeaders()).build();
        try {
            channel.basicPublish("", queueName, props, messageBytes);
            ret.setSuccess(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void listenOnQueueEvent(final String queueName, final Muon.EventMessageTransportListener listener) {
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    channel.queueDeclare(queueName, true, false, true, null);

                    log.fine("Waiting for point to point messages " + queueName);

                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(queueName, false, consumer);

                    while (true) {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        String message = new String(delivery.getBody());

                        log.finer("Received point to point: '" + message + "'");

                        MuonMessageEventBuilder builder = MuonMessageEventBuilder.named(queueName)
                                .withMimeType(delivery.getProperties().getContentType())
                                .withContent(message);

                        Map<Object, Object> headers = (Map) delivery.getProperties().getHeaders();

                        if (headers != null) {
                            for (Map.Entry<Object, Object> entry : headers.entrySet()) {
                                if (entry.getValue() == null) {
                                    log.warning("Value of " + entry.getKey() + " is null");

                                }
                                builder.withHeader(entry.getKey().toString(), entry.getValue().toString());
                            }
                        }

                        MuonMessageEvent ev = builder.build();

                        listener.onEvent(queueName, ev);

                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
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
