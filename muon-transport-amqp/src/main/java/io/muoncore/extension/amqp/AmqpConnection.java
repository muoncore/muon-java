package io.muoncore.extension.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;

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
