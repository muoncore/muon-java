package io.muoncore.extension.amqp;

import io.muoncore.Discovery;
import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

public class AmqpMessageTransformers {


    public static QueueListener.QueueMessage outboundToQueue(String queue, MuonOutboundMessage message, Codecs codecs, Discovery discovery) {

        Codecs.EncodingResult result = codecs.encode(message, discovery.getCodecsForService(message.getTargetServiceName()));

        return QueueMessageBuilder.queue(queue)
                .contentType(result.getContentType())
                .body(result.getPayload()).build();
    }

    public static MuonInboundMessage queueToInbound(QueueListener.QueueMessage message, Codecs codecs) {
        return codecs.decode(message.getBody(), message.getContentType(), MuonInboundMessage.class);
    }
}
