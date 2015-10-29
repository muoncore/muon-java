package io.muoncore.extension.amqp.crud.stream.server;


public class AmqpStreamControl /*implements OldMuon.EventMessageTransportListener */{
  /*  private Logger log = Logger.getLogger(AmqpStream.class.getName());

    public static final String COMMAND_REQUEST = "REQUEST";
    public static final String COMMAND_KEEP_ALIVE = "KEEP-ALIVE";
    public static final String COMMAND_SUBSCRIBE = "SUBSCRIBE";
    public static final String COMMAND_CANCEL = "CANCEL";
    public static final String REPLY_QUEUE_NAME = "REPLY_QUEUE_NAME";
    public static final String KEEPALIVE_QUEUE_NAME = "KEEPALIVE_QUEUE_NAME";
    public static final String REQUESTED_STREAM_NAME = "REQUESTED_STREAM_NAME";
    public static final String SUBSCRIPTION_STREAM_ID = "SUBSCRIPTION_STREAM_ID";
    public static final String REQUEST_COUNT = "N";
    public static final String SUBSCRIPTION_ACK = "SUBSCRIPTION_ACK";
    public static final String SUBSCRIPTION_NACK = "SUBSCRIPTION_NACK";
    private Map<String, MuonStreamGenerator> publisherStreams = Collections.synchronizedMap(new HashMap<String, MuonStreamGenerator>());
    private Map<String, AmqpProxySubscriber> subscriptions = Collections.synchronizedMap(new HashMap<String, AmqpProxySubscriber>());
    private ExecutorService spinner;

    private Map<String, Long> lastSeenKeepAlive = Collections.synchronizedMap(new HashMap<String, Long>());

    private AmqpQueues queues;
    private Codecs codecs;

    public AmqpStreamControl(final AmqpQueues queues, Codecs codecs) {
        this.queues = queues;
        this.codecs = codecs;
        spinner = Executors.newSingleThreadExecutor();
        monitorKeepAlive();
    }

    private void monitorKeepAlive() {
        final int KEEP_ALIVE_ERROR = 10000;
        //TODO, extract out into a monitor concept. This will vary hugely between transports.
        spinner.execute(() -> {
            while(true) {
                try {
                    Thread.sleep(1000);
                    Set<String> subscriberIds = new HashSet<>(subscriptions.keySet());
                    log.finer("Checking Subscriptions " + subscriptions);
                    long expiryTime = System.currentTimeMillis() - KEEP_ALIVE_ERROR;
                    for (String subId : subscriberIds) {
                        if (lastSeenKeepAlive.get(subId) < expiryTime) {
                            harvestBrokenStream(subId);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void harvestBrokenStream(String id) {
        log.warning("Subscription " + id + " has timed out of keep-alive, expiring and closing");
        AmqpProxySubscriber subscriber = subscriptions.remove(id);
        lastSeenKeepAlive.remove(id);
        subscriber.onError(new IOException("No KEEP-ALIVE received within the required time period. This subscription is now closed"));
        subscriber.cancel();
    }

    @Override
    public void onEvent(String name, MuonMessageEvent ev) {

        if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_KEEP_ALIVE)) {
            lastSeenKeepAlive.put((String) ev.getHeaders().get(SUBSCRIPTION_STREAM_ID), System.currentTimeMillis());
        } else if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_SUBSCRIBE)) {
            createNewSubscription(ev);
        } else if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_REQUEST)) {
            requestData(ev);
        } else if (ev.getHeaders().get(AmqpStream.STREAM_COMMAND).equals(COMMAND_CANCEL)) {
            cancelSubscription(ev);
        }
    }

    public Map<String, MuonStreamGenerator> getPublisherStreams() {
        return publisherStreams;
    }

    private synchronized void createNewSubscription(MuonMessageEvent ev) {
        //create a sub id
        String id = UUID.randomUUID().toString();

        String replyStreamName = (String) ev.getHeaders().get(REPLY_QUEUE_NAME);
        String keepAliveStreamName = (String) ev.getHeaders().get(KEEPALIVE_QUEUE_NAME);
        String requestedStreamName = (String) ev.getHeaders().get(REQUESTED_STREAM_NAME);

        AmqpProxySubscriber subscriber = new AmqpProxySubscriber(
                replyStreamName,
                keepAliveStreamName,
                queues, codecs);

        MuonStreamGenerator generator = publisherStreams.get(requestedStreamName);
        if (generator == null) {
            queues.send(replyStreamName,
                    MuonMessageEventBuilder.named("")
                            .withNoContent()
                            .withHeader(AmqpStream.STREAM_COMMAND, SUBSCRIPTION_NACK).build());
            return;
        }

        Publisher pub = generator.generatePublisher(ev.getHeaders());

        pub.subscribe(subscriber);

        lastSeenKeepAlive.put(id, System.currentTimeMillis());
        subscriptions.put(id, subscriber);

        log.finer("Created new subscription with id " + id + " to " + requestedStreamName);
        log.finer("Subscriptions now " + subscriptions);

        //send the sub id back to origin over replyTo queue.
        queues.send(replyStreamName,
                MuonMessageEventBuilder.named("")
                        .withNoContent()
                        .withHeader(AmqpStream.STREAM_COMMAND, SUBSCRIPTION_ACK)
                        .withHeader(AmqpStreamControl.SUBSCRIPTION_STREAM_ID, id).build());
        log.finer("Sent ACK for " + id);
    }

    private void requestData(MuonMessageEvent ev) {
        //lookup the sub
        String id = (String) ev.getHeaders().get(SUBSCRIPTION_STREAM_ID);
        String replyQueue = (String) ev.getHeaders().get(REPLY_QUEUE_NAME);

        AmqpProxySubscriber sub = subscriptions.get(id);

        if (sub == null) {
            queues.send(replyQueue,
                    MuonMessageEventBuilder.named("")
                            .withNoContent()
                            .withHeader(AmqpStream.STREAM_COMMAND, "ERROR").build());
        } else {
            long n = Long.parseLong((String) ev.getHeaders().get(REQUEST_COUNT));
            sub.request(n);
        }
    }

    private void cancelSubscription(MuonMessageEvent ev) {
        String id = (String) ev.getHeaders().get(SUBSCRIPTION_STREAM_ID);
        lastSeenKeepAlive.remove(id);
        log.fine("Removing subscriber " + id);
        AmqpProxySubscriber sub = subscriptions.remove(id);
        if (sub != null){
            sub.cancel();
        }
    }*/
}
