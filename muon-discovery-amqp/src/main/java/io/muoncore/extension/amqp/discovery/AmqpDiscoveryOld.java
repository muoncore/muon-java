package io.muoncore.extension.amqp.discovery;

public class AmqpDiscoveryOld /*implements Discovery */{

//    private static final String RESOURCE_CONNECTIONS = "resourceConnections";
//    private static final String STREAM_CONNECTIONS = "streamConnections";
//    private static final String IDENTIFIER = "identifier";
//    private static final String TAGS = "tags";
//    private Logger log = Logger.getLogger(AmqpDiscoveryOld.class.getName());
//    private ExecutorService spinner;
//    public static final String SERVICE_ANNOUNCE = "serviceAnnounce";
//
//    private ServiceCache serviceCache;
//    private AmqpConnection connection;
//    private ServiceDescriptor descriptor;
//    private final static int PING_TIME=3000;
//
//    private boolean isReady=false;
//    private List<Runnable> onReadyNotification = new ArrayList<>();
//
//    public AmqpDiscoveryOld(String amqpUrl) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {
//        this(new AmqpBroadcast(
//                new AmqpConnection(amqpUrl)));
//    }
//
//    public AmqpDiscoveryOld(AmqpBroadcast amqpBroadcast) {
//        this.amqpBroadcast = amqpBroadcast;
//        serviceCache = new ServiceCache();
//        spinner = Executors.newCachedThreadPool();
//        connect();
//    }
//
//    private Map decode(MuonMessageEvent event) {
//        return new TextBinaryCodec(new GsonTextCodec()).decode(
//                event.getBinaryEncodedContent());
//    }
//
//    private byte[] encode(MuonMessageEvent event) throws UnsupportedEncodingException {
//        return new GsonTextCodec().encode(event.getDecodedContent()).getBytes("UTF8");
//    }
//
//    public void connect() {
//        amqpBroadcast.listenOnBroadcastEvent(SERVICE_ANNOUNCE, new OldMuon.EventMessageTransportListener() {
//            @Override
//            public void onEvent(String name, MuonMessageEvent obj) {
//                Map announce = decode(obj);
//
//                serviceCache.addService(announce);
//            }
//        });
//
//        startAnnouncePing();
//        spinner.execute(() -> {
//            try {
//                Thread.sleep(PING_TIME + 200);
//                isReady=true;
//                notifyListeners();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//    }
//
//    private void notifyListeners() {
//        for (Runnable run: onReadyNotification) {
//            spinner.execute(run);
//        }
//    }
//
//    private void startAnnouncePing() {
//        spinner.execute(() -> {
//            try {
//                while (true) {
//                    if (descriptor != null) {
//                        Map<String,Object> discoveryMessage = new HashMap<>();
//
//                        discoveryMessage.put(IDENTIFIER, descriptor.getIdentifier());
//                        discoveryMessage.put(TAGS, descriptor.getTags());
//                        discoveryMessage.put(RESOURCE_CONNECTIONS, descriptor.getResourceConnectionUrls());
//                        discoveryMessage.put(STREAM_CONNECTIONS, descriptor.getStreamConnectionUrls());
//
//                        MuonMessageEvent ev = MuonMessageEventBuilder.named(SERVICE_ANNOUNCE)
//                                .withContent(discoveryMessage).build();
//
//                        ev.setEncodedBinaryContent(encode(ev));
//
//                        amqpBroadcast.broadcast(SERVICE_ANNOUNCE, ev);
//                    }
//                    Thread.sleep(3000);
//                }
//            } catch (InterruptedException | UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        });
//    }
//
//    @Override
//    public List<ServiceDescriptor> getKnownServices() {
//        List<ServiceDescriptor> services = new ArrayList<>();
//
//        try {
//            for (Map data : serviceCache.getServices()) {
//                List<URI> connectionList = readResourceConnectionUrls(data);
//                List<URI> streamConnectionList = readStreamConnectionUrls(data);
//
//                List tagList = readTags(data);
//
//                services.add(new ServiceDescriptor(
//                        (String) data.get(IDENTIFIER),
//                        tagList,
//                        Collections.<String>emptyList(), //TODO, more thought on how we collect/ use the codecs information
//                        connectionList,
//                        streamConnectionList));
//            }
//        } catch (URISyntaxException e) {
//            throw new IllegalArgumentException(
//                    "Unable to read a service descriptor, are you using the same protocol version?", e);
//        }
//
//        return services;
//    }
//
//    private List<URI> readStreamConnectionUrls(Map data) throws URISyntaxException {
//        return readConnectionUrls(STREAM_CONNECTIONS, data);
//    }
//
//    private List<URI> readResourceConnectionUrls(Map data) throws URISyntaxException {
//        return readConnectionUrls(RESOURCE_CONNECTIONS, data);
//    }
//
//    private List<URI> readConnectionUrls(String name, Map data) throws URISyntaxException {
//        Object connectionUrls = data.get(name);
//        List<URI> ret = new ArrayList<URI>();
//
//        if (connectionUrls != null && connectionUrls instanceof List) {
//            List urls = (List) connectionUrls;
//            for (Object url : urls) {
//                ret.add(new URI(url.toString()));
//            }
//        }
//        return ret;
//    }
//
//    private List readTags(Map data) {
//        Object tags = data.get(TAGS);
//        List ret = new ArrayList();
//
//        if (tags != null && tags instanceof List) {
//            ret = (List) tags;
//        }
//        return ret;
//    }
//
//    @Override
//    public void advertiseLocalService(ServiceDescriptor descriptor) {
//        this.descriptor = descriptor;
//    }
//
//    @Override
//    public void onReady(Runnable onReady) {
//        if (isReady) {
//            spinner.execute(onReady);
//        } else {
//            onReadyNotification.add(onReady);
//        }
//    }
}
