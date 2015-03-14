package io.muoncore.extension.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
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

        ConnectionFactory factory = new ConnectionFactory();

        factory.setUri(rabbitUrl);
        connection = factory.newConnection();

        channel = connection.createChannel();
    }

    public void close() {
        try {
            channel.close();
            connection.close();
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
