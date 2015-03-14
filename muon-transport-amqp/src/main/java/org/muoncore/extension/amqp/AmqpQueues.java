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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
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

        byte[] messageBytes = event.getBinaryEncodedContent();
        MuonService.MuonResult ret = new MuonService.MuonResult();

        event.getHeaders().put("Content Type", event.getContentType());

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().headers((Map) event.getHeaders()).build();
        try {
            channel.basicPublish("", queueName, props, messageBytes);
            ret.setSuccess(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public <T> void listenOnQueueEvent(final String queueName, Class<T> type, final Muon.EventMessageTransportListener listener) {
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
                        byte[] content = delivery.getBody();

                        MuonMessageEventBuilder builder = MuonMessageEventBuilder.named(queueName);

                        Map<String, Object> headers = delivery.getProperties().getHeaders();
                        if (headers == null) {
                            headers = new HashMap<String, Object>();
                        }
                        String contentType = delivery.getProperties().getContentType();

                        for (Map.Entry<String, Object> entry : headers.entrySet()) {
                            if (entry.getValue() != null) {
                                builder.withHeader(entry.getKey(), entry.getValue().toString());
                            }
                        }

                        MuonMessageEvent ev = builder.build();
                        ev.setContentType(contentType);
                        ev.setEncodedBinaryContent(content);
                        listener.onEvent(queueName, ev);

                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, e.getMessage(), e);
                }
            }
        });
    }

    public void shutdown() {
        spinner.shutdown();
    }
}
