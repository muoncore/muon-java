package org.muoncore.transport;

import com.rabbitmq.client.*;
import org.muoncore.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AMQPEventTransport implements MuonEventTransport {

    Connection connection;
    Channel channel;

    ExecutorService spinner;

    static String EXCHANGE_NAME ="muon-main";
    static String EXCHANGE_RES ="muon-resource";
    static String RABBIT_HOST = "localhost";

    public AMQPEventTransport() throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException, IOException {
        spinner = Executors.newCachedThreadPool();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqp://localhost:5672");
        //factory.setUri("amqp://userName:password@$RABBIT_HOST/");
        connection = factory.newConnection();

        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        channel.exchangeDeclare(EXCHANGE_RES, "fanout");
//        transport.queueDeclare(EXCHANGE_NAME, false, false, false, null);
    }

    @Override
    public MuonService.MuonResult emit(String eventName, MuonEvent event) {
        String payload = event.toString();
        byte[] messageBytes = payload.getBytes();

        MuonService.MuonResult ret = new MuonService.MuonResult();

        try {
            channel.basicPublish(EXCHANGE_NAME, eventName, null, messageBytes);

            ret.setSuccess(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public MuonService.MuonResult emitForReturn(String eventName, MuonEvent event) {
        String payload = event.toString();
        byte[] messageBytes = payload.getBytes();

        MuonService.MuonResult ret = new MuonService.MuonResult();

        try {

            String callbackQueueName = channel.queueDeclare().getQueue();

            //TODO, this generates a new queue for every single presend of this type. likely to break and is highly inefficient
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .replyTo(callbackQueueName)
                    .build();

            channel.basicPublish("", EXCHANGE_RES, props, messageBytes);

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(callbackQueueName, true, consumer);

            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());

            System.out.println("AMQP: Received Resource Reply '" + message + "'");

            String mimeType = delivery.getProperties().getContentType();
            Map<String, Object> head = delivery.getProperties().getHeaders();

            MuonEventBuilder builder = MuonEventBuilder.textMessage(message)
                    .withMimeType(mimeType);

            for(Map.Entry<String, Object> entry: head.entrySet()) {
                builder.withHeader(entry.getKey(), (String) entry.getValue());
            }

            ret.setEvent(builder.build());

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public void listenOnResource(final String resource, final String verb, final Muon.EventTransportListener listener) {
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                //TODO, add ability to filter on the verb, service name, resource ... (probably add it to the routing key)
                try {
                    channel.queueDeclare(EXCHANGE_RES, false, false, false, null);
                    channel.basicQos(1);

                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(EXCHANGE_RES, false, consumer);

                    System.out.println("AMQPChannel : Waiting for resource requests " + resource);

                    while (true) {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                        System.out.println("AMQPChannel : Got Request " + resource);

                        BasicProperties props = delivery.getProperties();
                        AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                                .correlationId(props.getCorrelationId())
                                .build();

                        String message = new String(delivery.getBody());

                        System.out.println("AMQPChannel : Received " + message);

                        //todo, transport marshalling
                        String response = listener.onEvent(resource, message).toString();
                        System.out.println("AMQPChannel : Sending" + response);

                        channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes());

                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void listenOnEvent(final String resource, final Muon.EventTransportListener listener) {
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String queueName = null;
                    queueName = channel.queueDeclare().getQueue();

                    channel.queueBind(queueName, EXCHANGE_NAME, resource);

                    System.out.println("AMQPChannel : Waiting for messages " + resource);

                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(queueName, true, consumer);

                    while (true) {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        String message = new String(delivery.getBody());

                        System.out.println("AMQP: Received '" + message + "'");

                        listener.onEvent(resource,
                                message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public List<ServiceDescriptor> discoverServices() {
        throw new IllegalStateException("Not Implemented");
    }
}
