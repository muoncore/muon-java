package io.muoncore.message;

public class MuonInboundMessage extends MuonMessage {

    public MuonInboundMessage(String id, long created, String targetServiceName, String sourceServiceName, String protocol, String step, Status status, byte[] payload, String contentType, ChannelOperation channelOperation) {
        super(id, created, targetServiceName, sourceServiceName, protocol, step, status, payload, contentType, channelOperation);
    }

    public static MuonInboundMessage serviceNotFound(MuonOutboundMessage msg) {
        return new MuonInboundMessage(
                msg.getId(),
                System.currentTimeMillis(),
                msg.getSourceServiceName(),
                msg.getTargetServiceName(),
                msg.getProtocol(),
                msg.getStep(),
                Status.failure,
                msg.getPayload(),
                msg.getContentType(),
                ChannelOperation.NORMAL
        );
    }
}
