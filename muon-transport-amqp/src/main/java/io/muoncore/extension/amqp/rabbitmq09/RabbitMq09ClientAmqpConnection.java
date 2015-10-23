package io.muoncore.extension.amqp.rabbitmq09;

import com.rabbitmq.client.*;
import io.muoncore.exception.MuonException;
import io.muoncore.extension.amqp.AmqpConnection;
import io.muoncore.extension.amqp.QueueListener;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RabbitMq09ClientAmqpConnection implements AmqpConnection {

    private Logger log = Logger.getLogger(RabbitMq09ClientAmqpConnection.class.getName());

    private Connection connection;
    private Channel channel;

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
                    factory.setUri(rabbitUrl);
                    connection = factory.newConnection();
                    channel = connection.createChannel();
                    reconnect = false;
                    synchronized (factory) {
                        factory.notify();
                    }
                } catch (ConnectException e) {
                    log.warning("Unable to connect to rabbit server " + rabbitUrl + " retrying");
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
    public void send(QueueListener.QueueMessage message) throws IOException {

        log.info("Sending message on " + message.getQueueName() + " of type " + message.getEventType());

        Map<String, Object> headers = new HashMap<>(message.getHeaders());
        headers.put("eventType", message.getEventType());

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .contentType(message.getContentType())
                .headers(headers).build();

        channel.basicPublish("", message.getQueueName(), props, message.getBody());
    }

    @Override
    public boolean isAvailable() {
        return connection != null;
    }

    @Override
    public void close() {
        try {
            channel.close();
            connection.close();
            Thread.sleep(1000);
        } catch (ShutdownSignalException ex) {
            if (ex.isHardError()) {
                log.log(Level.WARNING, ex.getMessage(), ex);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }
    }
}
