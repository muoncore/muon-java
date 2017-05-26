package io.muoncore.protocol.rpc.server;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.ServiceDescriptor;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.descriptors.OperationDescriptor;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.descriptors.SchemaDescriptor;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.ServerJSProtocol;
import io.muoncore.protocol.ServerProtocolStack;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Server side of the Requestr Response protocol.
 * <p>
 * Transports open channels on this protocol when a remote request response client opens a channel through them
 * and sends a first message.
 */
@Slf4j
@AllArgsConstructor
public class RequestResponseServerProtocolStack implements
  ServerProtocolStack {

  private static final Logger LOG = LoggerFactory.getLogger(RequestResponseServerProtocolStack.class.getCanonicalName());
  private final RequestResponseHandlers handlers;
  private Muon muon;

  @Override
  @SuppressWarnings("unchecked")
  public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel() {

    Channel<MuonOutboundMessage, MuonInboundMessage> api2 = Channels.workerChannel("rrpserver", "transport");

    ServerJSProtocol proto = new ServerJSProtocol(muon, "rpc", api2.left());
    proto.addTypeForDecoding("ServerRequest", ServerRequest.class);
    proto.addPostDecodingDecorator(ServerRequest.class, serverRequest -> {
      serverRequest.setCodecs(muon.getCodecs());
      return serverRequest;
    });


    Function<ServerRequest, Consumer> function = (ServerRequest request) -> {
      return (Consumer<ScriptObjectMirror>) callbackOnResponse -> {

        RequestResponseServerHandler handler = handlers.findHandler(request);

        handler.handle(new RequestWrapper() {
          @Override
          public ServerRequest getRequest() {
            return request;
          }

          @Override
          public void answer(ServerResponse response) {
            log.info("Response has been generated");
            callbackOnResponse.call(null, response);
          }
        });
      };
    };

    proto.setState("getHandler", function);

    proto.start(RequestResponseServerProtocolStack.class.getResourceAsStream("/rpc-server.js"));

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
      "rpc",
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
