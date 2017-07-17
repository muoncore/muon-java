package io.muoncore.message;

import io.muoncore.transport.TransportEvents;

public class MuonInboundMessage extends MuonMessage {

    public MuonInboundMessage(String id, long created, String targetServiceName, String targetInstance, String sourceServiceName, String protocol, String step, Status status, byte[] payload, String contentType, ChannelOperation channelOperation) {
        super(id, created, targetServiceName, targetInstance, sourceServiceName, protocol, step, status, payload, contentType, channelOperation);
    }

    public static MuonInboundMessage serviceNotFound(MuonOutboundMessage msg) {
        return new MuonInboundMessage(
                msg.getId(),
                System.currentTimeMillis(),
                msg.getSourceServiceName(),
                msg.getTargetInstance(),
                msg.getTargetServiceName(),
                msg.getProtocol(),
                TransportEvents.SERVICE_NOT_FOUND,
                Status.failure,
                msg.getPayload(),
                msg.getContentType(),
                ChannelOperation.normal
        );
    }
}
