package io.muoncore.extension.amqp.crud.stream;

public class AmqpBroadcast {
/*
    static String EXCHANGE_NAME ="muon-broadcast";

    private Logger log = Logger.getLogger(AmqpBroadcast.class.getName());

    private Channel channel;
    private ExecutorService spinner;

    public AmqpBroadcast(AmqpConnection connection) throws IOException {
        this.channel = connection.getChannel();
        spinner = Executors.newCachedThreadPool();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
    }

    public MuonService.MuonResult broadcast(String eventName, MuonMessageEvent event) {
        byte[] messageBytes = event.getBinaryEncodedContent();

        MuonService.MuonResult ret = new MuonService.MuonResult();

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .contentType(event.getContentType())
                .headers(event.getHeaders()).build();

        try {
            channel.basicPublish(EXCHANGE_NAME, eventName, props, messageBytes);

            ret.setSuccess(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public void listenOnBroadcastEvent(final String resource, final OldMuon.EventMessageTransportListener listener) {
        spinner.execute(() -> {
            try {
                String queueName = null;
                queueName = channel.queueDeclare().getQueue();

                channel.queueBind(queueName, EXCHANGE_NAME, resource);

                log.fine("Waiting for discovery broadcast messages " + resource);

                QueueingConsumer consumer = new QueueingConsumer(channel);
                channel.basicConsume(queueName, true, consumer);

                while (true) {
                    try {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        byte[] body = delivery.getBody();

                        MuonMessageEventBuilder builder = MuonMessageEventBuilder.named(resource);
//                                .withMimeType(delivery.getProperties().getContentType())

                        Map<Object, Object> headers = (Map) delivery.getProperties().getHeaders();

                        if (headers != null) {
                            for (Map.Entry<Object, Object> entry : headers.entrySet()) {
                                builder.withHeader(entry.getKey().toString(), entry.getValue().toString());
                            }
                        }

                        MuonMessageEvent ev = builder.build();
                        ev.setContentType(delivery.getProperties().getContentType());
                        ev.setEncodedBinaryContent(body);

                        listener.onEvent(resource, ev);
                    } catch (ShutdownSignalException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        spinner.shutdown();
    }*/
}