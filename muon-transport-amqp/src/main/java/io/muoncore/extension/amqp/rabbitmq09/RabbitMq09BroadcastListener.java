package io.muoncore.extension.amqp.rabbitmq09;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import io.muoncore.extension.amqp.QueueListener;
import io.muoncore.extension.amqp.QueueMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RabbitMq09BroadcastListener implements QueueListener {

    private boolean running;
    private Channel channel;
    private Logger log = LoggerFactory.getLogger(RabbitMq09BroadcastListener.class.getName());
    private String broadcastMessageType;
    private String queueName;
    private QueueFunction listener;
    private QueueingConsumer consumer;

    public RabbitMq09BroadcastListener(Channel channel, String broadcastMessageType, QueueFunction function) {
        this.channel = channel;
        this.broadcastMessageType = broadcastMessageType;
        this.listener = function;
    }

    public void blockUntilReady() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    public void start() {
        new Thread(RabbitMq09BroadcastListener.this::run).start();
    }

    public void run() {
        try {
            channel.exchangeDeclare("muon-broadcast", "topic");

            queueName = channel.queueDeclare().getQueue();

            channel.queueBind(queueName, "muon-broadcast", broadcastMessageType);

            synchronized (this) {
                notify();
            }

            consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, false, consumer);

            running = true;
            while (running) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    byte[] content = delivery.getBody();

                    Map<String, Object> headers = delivery.getProperties().getHeaders();

                    if (headers == null) {
                        headers = new HashMap<>();
                    }

                    Map<String, String> newHeaders = new HashMap<>();

                    headers.entrySet().stream().forEach( entry -> newHeaders.put(entry.getKey(), entry.getValue().toString()));
                    newHeaders.putIfAbsent(QueueMessageBuilder.HEADER_CONTENT_TYPE, headers.get("Content-Type").toString());
                    listener.exec(new QueueMessage(broadcastMessageType, content, newHeaders));

                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (ShutdownSignalException | ConsumerCancelledException ex) {
                    log.debug("Channel shuts down", ex);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        log.debug("Broadcast Listener exits: " + broadcastMessageType);
    }

    public void cancel() {
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
