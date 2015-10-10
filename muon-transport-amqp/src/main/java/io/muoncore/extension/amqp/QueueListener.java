package io.muoncore.extension.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import io.muoncore.crud.OldMuon;
import io.muoncore.transport.crud.MuonMessageEvent;
import io.muoncore.transport.crud.MuonMessageEventBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueueListener implements Runnable {

    private boolean running;
    private Channel channel;
    private Logger log = Logger.getLogger(QueueListener.class.getName());
    private String queueName;
    private OldMuon.EventMessageTransportListener listener;
    private QueueingConsumer consumer;

    public QueueListener(Channel channel, String queueName, OldMuon.EventMessageTransportListener listener) {
        this.channel = channel;
        this.queueName = queueName;
        this.listener = listener;
    }

    public void blockUntilReady() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    @Override
    public void run() {
        try {
            log.info("Opening Queue: " + queueName);
            channel.queueDeclare(queueName, false, false, true, null);

            synchronized (this) {
                notify();
            }

            consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, false, consumer);
            log.info("Queue ready: " + queueName);

            running = true;
            while (running) {
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
                } catch (ShutdownSignalException ex) {
                    log.log(Level.FINER, ex.getMessage(), ex);
                } catch (ConsumerCancelledException ex) {
                    log.log(Level.FINER, ex.getMessage(), ex);
                } catch (Exception e) {
                    log.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }
        log.warning("Queue Listener exits: " + queueName);
    }

    public void cancel() {
        log.info("Queue listener is cancelled:" + queueName);
        running = false;
        try {
            consumer.handleCancel("Muon-Cancel");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                channel.queueDelete(queueName, false, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
