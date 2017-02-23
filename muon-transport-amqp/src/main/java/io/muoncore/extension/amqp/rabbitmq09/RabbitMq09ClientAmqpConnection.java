package io.muoncore.extension.amqp.rabbitmq09;

import com.rabbitmq.client.*;
import io.muoncore.exception.MuonException;
import io.muoncore.extension.amqp.AmqpConnection;
import io.muoncore.extension.amqp.QueueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMq09ClientAmqpConnection implements AmqpConnection {

    private Logger log = LoggerFactory.getLogger(RabbitMq09ClientAmqpConnection.class.getName());

    private Connection connection;
    private Channel channel;

    @Override
    public void deleteQueue(String queue) {
        try {
            channel.queueDelete(queue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    public RabbitMq09ClientAmqpConnection(String rabbitUrl)
            throws IOException,
            NoSuchAlgorithmException,
            KeyManagementException,
            URISyntaxException {
        final ConnectionFactory factory = new ConnectionFactory();

        new Thread(() -> {
            boolean reconnect = true;
            while (reconnect) {
                try {
                    log.info("Connecting to AMQP broker using url: " + rabbitUrl);
                    factory.setUri(rabbitUrl);
                    connection = factory.newConnection();
                    channel = connection.createChannel();

                    channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> {
                        log.trace("Message has returned on queue: " + routingKey);
                    });
                    reconnect = false;
                    synchronized (factory) {
                        factory.notify();
                    }
                } catch (TimeoutException | ConnectException e) {
                    log.warn("Unable to connect to rabbit server " + rabbitUrl + " retrying");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException | URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
                    reconnect = false;
                    e.printStackTrace();
                    synchronized (factory) {
                        factory.notify();
                    }
                }
            }
        }).start();
        try {
            synchronized (factory) {
                factory.wait(60000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (channel == null) {
            throw new MuonException("Unable to connect to remote rabbit within 60s, failing");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void send(QueueListener.QueueMessage message) throws IOException {

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
//                .contentType(message.getContentType())

                .headers((Map) message.getHeaders()).build();

        channel.basicPublish("", message.getQueueName(), true, props, message.getBody());
    }

    @Override
    public void broadcast(QueueListener.QueueMessage message) throws IOException {
        byte[] messageBytes = message.getBody();

        Map<String, Object> headers = new HashMap<>(message.getHeaders());
        headers.put("Content-Type", message.getContentType());

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .contentType(message.getContentType())
                .headers(headers).build();

        channel.basicPublish("muon-broadcast", message.getQueueName(), props, messageBytes);
    }

    @Override
    public boolean isAvailable() {
        return connection != null;
    }

    @Override
    public void close() {
        try {
            if (channel.isOpen()) {
                channel.close();
            }
            if (connection.isOpen()) {
                connection.close();
            }
        } catch (ShutdownSignalException ex) {
            if (ex.isHardError()) {
                log.warn(ex.getMessage(), ex);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }
}
