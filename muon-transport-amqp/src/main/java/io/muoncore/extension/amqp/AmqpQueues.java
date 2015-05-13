package io.muoncore.extension.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import io.muoncore.Muon;
import io.muoncore.MuonClient;
import io.muoncore.MuonService;
import io.muoncore.transport.MuonMessageEventBuilder;
import io.muoncore.transport.MuonMessageEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AmqpQueues {

    private ExecutorService spinner;
    private Channel channel;
    private Map<Muon.EventMessageTransportListener, QueueListener> listeners;

    public AmqpQueues(Channel channel) throws IOException {
        this.channel = channel;
        spinner = Executors.newCachedThreadPool();
        listeners = new HashMap<Muon.EventMessageTransportListener, QueueListener>();
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

    public void removeListener(Muon.EventMessageTransportListener listener) {
        QueueListener queueListener = listeners.remove(listener);
        if (queueListener != null) {
            queueListener.cancel();
        }
    }

    public <T> void listenOnQueueEvent(final String queueName, Class<T> type, final Muon.EventMessageTransportListener listener) {
        QueueListener queueListener = new QueueListener(
                channel, queueName, listener
        );
        listeners.put(listener, queueListener);
        spinner.execute(queueListener);
    }

    public void shutdown() {
        spinner.shutdown();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
