package io.muoncore.transport;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.Codecs;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerStacks;

import java.net.URI;

public interface MuonTransport {

    void shutdown();

    void start(
            Discovery discovery,
            ServerStacks serverStacks, Codecs codecs, Scheduler scheduler) throws MuonTransportFailureException;

    String getUrlScheme();

    URI getLocalConnectionURI();

    boolean canConnectToService(String name);

    ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel(
            String serviceName,
            String protocol) throws NoSuchServiceException, MuonTransportFailureException;
}
