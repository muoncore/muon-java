package io.muoncore.extension.amqp.stream.client;


public class AmqpStreamClient<T>/* implements
        OldMuon.EventMessageTransportListener,
        Subscription*/ {
  /*  private AmqpQueues queues;
    private String streamName;
    private String privateStreamQueue;
    private String privateKeepAliveQueue;
    private String commandQueue;
    private Subscriber<T> subscriber;
    private Class<T> type;
    private Codecs codecs;

    private String remoteId;

    private ScheduledExecutorService keepAliveScheduler;

    private Logger log = Logger.getLogger(AmqpStreamClient.class.getName());

    private long lastSeenKeepAlive;

    private boolean isTerminated = false;

    public AmqpStreamClient(
            String commandQueue,
            String streamName,
            Map<String, String> params,
            Subscriber<T> subscriber,
            Class<T> type,
            Codecs codecs,
            AmqpQueues queues) {
        this.queues = queues;
        this.subscriber = subscriber;
        this.commandQueue = commandQueue;
        this.streamName = streamName;
        this.type = type;
        this.codecs = codecs;

        privateStreamQueue = UUID.randomUUID().toString();
        privateKeepAliveQueue = UUID.randomUUID().toString();
        queues.listenOnQueueEvent(privateStreamQueue, Void.class, this);
        queues.listenOnQueueEvent(privateKeepAliveQueue, Void.class, this);
        log.info("Listening for events from the remote on " + privateStreamQueue);

        MuonMessageEvent ev = MuonMessageEventBuilder.named("")
                .withNoContent()
                .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_SUBSCRIBE)
                .withHeader(AmqpStreamControl.REQUESTED_STREAM_NAME, streamName)
                .withHeader(AmqpStreamControl.REPLY_QUEUE_NAME, privateStreamQueue)
                .withHeader(AmqpStreamControl.KEEPALIVE_QUEUE_NAME, privateKeepAliveQueue).build();

        //TODO, an event should be emitted bu the listenOnQueue above. We need to know when the queue is ready
        //otherwise we end up in a race with the remote service
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ev.getHeaders().putAll(params);
        //TODO, necessary?
        ev.setContentType("application/json");
        queues.send(commandQueue, ev);

        lastSeenKeepAlive = System.currentTimeMillis() + 3000;
    }

    public String getStreamName() {
        return streamName;
    }

    public boolean isTerminated() {
        return isTerminated;
    }

    @Override
    public void onEvent(String name, MuonMessageEvent obj) {
        log.finer("Received message " + obj.getHeaders().get(AmqpStream.STREAM_COMMAND) + " on queue " + name);
        if (obj.getHeaders().get(AmqpStream.STREAM_COMMAND) != null &&
                obj.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(AmqpStreamControl.SUBSCRIPTION_ACK)) {
            remoteId = (String) obj.getHeaders().get(AmqpStreamControl.SUBSCRIPTION_STREAM_ID);
            log.fine("Received SUBSCRIPTION_ACK " + remoteId + " activating local subscription");
            lastSeenKeepAlive = System.currentTimeMillis();
            sendKeepAlive();
            subscriber.onSubscribe(this);
        } else if (obj.getHeaders().get(AmqpStream.STREAM_COMMAND) != null &&
                    obj.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(AmqpStreamControl.SUBSCRIPTION_NACK)) {
            log.warning("SUBSCRIPTION_NACK for stream [" + streamName + "] stream is NOT established");
            subscriber.onError(new IllegalArgumentException("SUBSCRIPTION_NACK for stream [" + streamName + "] stream is NOT established"));
        } else if (obj.getHeaders().get(AmqpStream.STREAM_COMMAND) != null &&
                obj.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(AmqpStreamControl.COMMAND_KEEP_ALIVE)) {
            lastSeenKeepAlive = System.currentTimeMillis();
        } else if (obj.getHeaders().get("TYPE").equals("data")) {
            byte[] data = obj.getBinaryEncodedContent();
            EventLogger.logEvent(streamName, obj);
            T decodedObject = codecs.decodeObject(data, obj.getContentType(), type);
            subscriber.onNext(decodedObject);
        } else if (obj.getHeaders().get("TYPE").equals("error")) {
            log.warning(streamName+ " " + remoteId + ":Error reported by remote : " + obj.getHeaders().get("ERROR"));
            shutdown();
            onError(new IOException((String) obj.getHeaders().get("ERROR")));
        } else if (obj.getHeaders().get("TYPE").equals("complete")) {
            log.info(streamName+ " " + remoteId + ":Remote reports stream is completed");
            shutdown();
            subscriber.onComplete();
        } else {
            log.warning("Received an unknown message on the control channel " + obj.getHeaders());
        }
    }

    public void onError(Exception ex) {
        shutdown();
        subscriber.onError(ex);
    }

    public long getLastSeenKeepAlive() {
        return lastSeenKeepAlive;
    }

    @Override
    public void request(long n) {
        //request the remote publisher to send more data
        log.fine("Requesting " + n + " more data from server " + remoteId);
        queues.send(commandQueue,
                MuonMessageEventBuilder.named("")
                        .withNoContent()
                        .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_REQUEST)
                        .withHeader(AmqpStreamControl.REQUEST_COUNT, String.valueOf(n))
                        .withHeader(AmqpStreamControl.SUBSCRIPTION_STREAM_ID, remoteId).build());
    }

    private void sendKeepAlive() {
        if (keepAliveScheduler != null) {
            keepAliveScheduler.shutdownNow();
        }
        keepAliveScheduler = Executors.newScheduledThreadPool(1);
        Runnable keepAliveSender = new Runnable() {
            @Override
            public void run() {
                log.fine("Sending client keep alive : " + remoteId);
                MuonMessageEvent ev = MuonMessageEventBuilder.named(
                        "")
                        .withNoContent()
                        .withHeader(AmqpStreamControl.SUBSCRIPTION_STREAM_ID, remoteId)
                        .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_KEEP_ALIVE)
                        .build();

                AmqpStreamClient.this.queues.send(
                        commandQueue,
                        ev);
            }
        };

        keepAliveScheduler.scheduleAtFixedRate(keepAliveSender, 0, 1, TimeUnit.SECONDS);
    }

    private void shutdown() {
        if (keepAliveScheduler != null) {
            keepAliveScheduler.shutdownNow();
            keepAliveScheduler = null;
            isTerminated = true;
        }
        queues.removeListener(this);
    }

    @Override
    public void cancel() {
        shutdown();
        queues.send(commandQueue,
                MuonMessageEventBuilder.named("")
                        .withNoContent()
                        .withHeader(AmqpStream.STREAM_COMMAND, AmqpStreamControl.COMMAND_CANCEL)
                        .withHeader(AmqpStreamControl.SUBSCRIPTION_STREAM_ID, remoteId).build());
        log.info("Subscription " + remoteId + " has been cancelled, the remote has been notified");
    }*/
}
