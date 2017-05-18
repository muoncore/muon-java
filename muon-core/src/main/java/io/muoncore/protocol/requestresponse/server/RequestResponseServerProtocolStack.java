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
import io.muoncore.descriptors.SchemaDescriptor;
import io.muoncore.descriptors.SchemasDescriptor;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.protocol.requestresponse.RRPTransformers;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Server side of the Requestr Response protocol.
 * <p>
 * Transports open channels on this protocol when a remote request response client opens a channel through them
 * and sends a first message.
 */
@AllArgsConstructor
public class RequestResponseServerProtocolStack implements
  ServerProtocolStack {

  private static final Logger LOG = LoggerFactory.getLogger(RequestResponseServerProtocolStack.class.getCanonicalName());
  private final RequestResponseHandlers handlers;
  private Codecs codecs;
  private Discovery discovery;
  private AutoConfiguration config;

  @Override
  @SuppressWarnings("unchecked")
  public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel() {

    Channel<MuonOutboundMessage, MuonInboundMessage> api2 = Channels.workerChannel("rrpserver", "transport");

    api2.left().receive(message -> {
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
            LOG.warn("Could not locate service " + request.getUrl().getHost() + ", setting response codec to application/json");
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

  @Override
  public Map<String, SchemaDescriptor> getSchemasFor(String endpoint) {

    Optional<RequestResponseServerHandler> first = handlers.getHandlers().stream().filter(requestResponseServerHandler ->
      requestResponseServerHandler.getPredicate().resourceString().equals(endpoint)).findFirst();

    return first.map(RequestResponseServerHandler::getDescriptors).orElseGet(Collections::emptyMap);

  }
}
