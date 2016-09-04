package io.muoncore.transport.sharedsocket.client.messages;

import io.muoncore.message.MuonInboundMessage;

public class SharedChannelInboundMessage {

    private String channelId;
    private byte[] payload;

    public MuonInboundMessage getMessage() {
        return null;
    }

    public String getChannelId() {
        return channelId;
    }
}
