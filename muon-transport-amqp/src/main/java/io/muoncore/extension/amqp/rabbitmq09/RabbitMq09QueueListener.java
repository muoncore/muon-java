package io.muoncore.extension.amqp.rabbitmq09;


import com.rabbitmq.client.*;
import io.muoncore.extension.amqp.QueueListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RabbitMq09QueueListener implements QueueListener {

    private boolean running;
    private Channel channel;
    private Logger log = Logger.getLogger(RabbitMq09QueueListener.class.getName());
    private String queueName;
    private QueueListener.QueueFunction listener;
    private Consumer consumer;
    private CountDownLatch latch = new CountDownLatch(1);

    public RabbitMq09QueueListener(Channel channel, String queueName, QueueListener.QueueFunction function) {
        this.channel = channel;
        this.queueName = queueName;
        this.listener = function;
    }

    public void blockUntilReady() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, "Error waiting for amqp listener to start", e);
        }
    }

    public void start() {
        run();
    }

    public void run() {
        try {
            log.log(Level.FINE, "Opening Queue: " + queueName);
            channel.queueDeclare(queueName, false, false, true, null);

            consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    try {

                        Map<String, Object> headers = properties.getHeaders();

                        if (headers == null) {
                            headers = new HashMap<>();
                        }

                        Map<String, String> newHeaders = new HashMap<>();
                        headers.entrySet().stream().forEach( entry -> {
                            if (entry.getKey() == null || entry.getValue() == null) {
                                return;
                            }
                            newHeaders.put(entry.getKey(), entry.getValue().toString());
                        });

                        log.log(Level.FINE, "Receiving message on " + queueName + " of type " + newHeaders.get("eventType"));

                        listener.exec(new QueueListener.QueueMessage(queueName, body, newHeaders));

                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (ShutdownSignalException | ConsumerCancelledException ex) {
                        log.log(Level.FINER, ex.getMessage(), ex);
                    } catch (Exception e) {
                        log.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            };

            channel.basicConsume(queueName, false, consumer);

            latch.countDown();

            log.log(Level.FINE, "Queue ready: " + queueName);

        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }
        log.log(Level.FINE, "Queue Listener exits: " + queueName);
    }

    public void cancel() {
        log.log(Level.FINE, "Queue listener is cancelled:" + queueName);
        running = false;
        try {
            consumer.handleCancel("Muon-Cancel");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                channel.queueDelete(queueName, false, false);
            } catch (IOException | AlreadyClosedException e) {
                e.printStackTrace();
            }
        }
    }
}
