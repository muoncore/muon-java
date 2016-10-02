package io.muoncore.transport.sharedsocket.client.messages;

import io.muoncore.message.MuonInboundMessage;

public class SharedChannelInboundMessage {

    private String channelId;
    private MuonInboundMessage message;

    public MuonInboundMessage getMessage() {
        return message;
    }

    public String getChannelId() {
        return channelId;
    }
}
