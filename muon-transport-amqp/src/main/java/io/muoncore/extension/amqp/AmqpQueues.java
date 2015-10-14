package io.muoncore.extension.amqp;


public class AmqpQueues {

 /*   final private ExecutorService spinner;
    private Channel channel;
    private Map<OldMuon.EventMessageTransportListener, QueueListener> listeners;

    public AmqpQueues(Channel channel) throws IOException {
        this.channel = channel;
        spinner = Executors.newCachedThreadPool();
        listeners = Collections.synchronizedMap(new HashMap<OldMuon.EventMessageTransportListener, QueueListener>());
    }

    public MuonClient.MuonResult send(String queueName, MuonMessageEvent event) {
        byte[] messageBytes = event.getBinaryEncodedContent();
        if (messageBytes == null) {
            messageBytes = new byte[0];
        }
        MuonService.MuonResult ret = new MuonService.MuonResult();

        String contentType = event.getContentType();
        if (contentType == null) {
            contentType = "application/json";
        }
        event.getHeaders().put("Content-Type", contentType);

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().
                contentType(contentType)
                .headers((Map) event.getHeaders()).build();


        try {
            channel.basicPublish("", queueName, props, messageBytes);
            ret.setSuccess(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void removeListener(OldMuon.EventMessageTransportListener listener) {
        synchronized (spinner) {
            QueueListener queueListener = listeners.remove(listener);
            if (queueListener != null) {
                queueListener.cancel();
            }
        }
    }

    public <T> void listenOnQueueEvent(final String queueName, Class<T> type, final OldMuon.EventMessageTransportListener listener) {
        synchronized (spinner) {
            QueueListener queueListener = new QueueListener(
                    channel, queueName, listener
            );
            listeners.put(listener, queueListener);
//            spinner.execute(queueListener);
            new Thread(queueListener).start();
            queueListener.blockUntilReady();
        }
    }

    public void shutdown() {
        synchronized (spinner) {
            spinner.shutdown();
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}
