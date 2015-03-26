package io.muoncore.extension.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import io.muoncore.Muon;
import io.muoncore.MuonClient;
import io.muoncore.MuonService;
import io.muoncore.transport.MuonMessageEventBuilder;
import io.muoncore.transport.MuonMessageEvent;

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
        //TODO, one of the many sucky things about this mish mash design.
        if (messageBytes == null) {
            messageBytes = "{}".getBytes();
        }
        MuonService.MuonResult ret = new MuonService.MuonResult();

        String contentType = event.getContentType();
        if (contentType == null) {
            contentType = "application/json";
        }
        event.getHeaders().put("Content-Type", contentType);

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
                        try {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                            byte[] content = delivery.getBody();

                            MuonMessageEventBuilder builder = MuonMessageEventBuilder.named(queueName);

                            Map<String, Object> headers = delivery.getProperties().getHeaders();
                            if (headers == null) {
                                headers = new HashMap<String, Object>();
                            }
                            String contentType = "";
                            if (headers.get("Content-Type") != null) {
                                contentType = headers.get("Content-Type").toString();
                            }

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
                        } catch (Exception e) {
                            log.log(Level.WARNING, e.getMessage(), e);
                            ///TODO, send an error?
                        }
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
