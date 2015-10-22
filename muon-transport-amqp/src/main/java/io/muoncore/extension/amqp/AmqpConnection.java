package io.muoncore.extension.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;
import io.muoncore.exception.MuonException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AmqpConnection {

    private Logger log = Logger.getLogger(AmqpConnection.class.getName());

    private Connection connection;
    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public AmqpConnection(String rabbitUrl)
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

    public boolean isAvailable() {
        return connection != null;
    }

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
