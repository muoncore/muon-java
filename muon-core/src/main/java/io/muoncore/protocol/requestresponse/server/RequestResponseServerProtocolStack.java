package io.muoncore.protocol.requestresponse.server;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.descriptors.OperationDescriptor;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.protocol.requestresponse.RRPTransformers;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Server side of the Requestr Response protocol.
 *
 * Transports open channels on this protocol when a remote request response client opens a channel through them
 * and sends a first message.
 */
public class RequestResponseServerProtocolStack implements
        ServerProtocolStack {

    private static final Logger LOG = Logger.getLogger(RequestResponseServerProtocolStack.class.getCanonicalName());
    private final RequestResponseHandlers handlers;
    private Codecs codecs;
    private Discovery discovery;
    private AutoConfiguration config;

    public RequestResponseServerProtocolStack(RequestResponseHandlers handlers,
                                              Codecs codecs,
                                              Discovery discover,
                                              AutoConfiguration config) {
        this.codecs = codecs;
        this.handlers = handlers;
        this.discovery = discover;
        this.config = config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel() {

        Channel<MuonOutboundMessage, MuonInboundMessage> api2 = Channels.workerChannel("rrpserver", "transport");

        api2.left().receive( message -> {
            if (message == null || message.getChannelOperation() == MuonMessage.ChannelOperation.closed) {
                //shutdown signal.
                return;
            }

            final ServerRequest request = RRPTransformers.toRequest(message, codecs);
            final RequestResponseServerHandler handler = handlers.findHandler(request);

            handler.handle(new RequestWrapper() {
                @Override
                public ServerRequest getRequest() {
                    return request;
                }

                @Override
                public void answer(ServerResponse response) {
                    Optional<ServiceDescriptor> target = discovery.findService(svc ->
                            svc.getIdentifier().equals(
                                    config.getServiceName()));

                    String[] codecList;
                    if (target.isPresent()) {
                        codecList = target.get().getCodecs();
                    } else {
                        LOG.log(Level.WARNING, "Could not locate service " + request.getUrl().getHost() + ", setting response codec to application/json");
                        codecList = new String[]{"application/json"};
                    }

                    MuonOutboundMessage msg = RRPTransformers.toOutbound(config.getServiceName(),
                            request.getUrl().getHost(), response, codecs,
                            codecList);

                    api2.left().send(msg);
                }
            });
        });

        return api2.right();
    }

    @Override
    public ProtocolDescriptor getProtocolDescriptor() {

        List<OperationDescriptor> ops =
                handlers.getHandlers().stream()
                        .map(
                handler -> new OperationDescriptor(handler.getPredicate().resourceString()))
                .collect(Collectors.toList());

        return new ProtocolDescriptor(
                RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                "Request/ Response Protocol",
                "Make a single request, get a single response",
                ops);
    }
}
