package io.muoncore.protocol.rpc.server;

import io.muoncore.Muon;
import io.muoncore.codec.Codecs;
import io.muoncore.descriptors.SchemaDescriptor;

import java.util.Collections;
import java.util.Map;

public class RpcServer implements RequestResponseHandlersSource, RequestResponseServerHandlerApi {

  private RequestResponseHandlers requestResponseHandlers;
  private Muon muon;

  public RpcServer(Muon muon) {
    this.muon = muon;
    initDefaultRequestHandler();
    muon.getProtocolStacks().registerServerProtocol(new RequestResponseServerProtocolStack(
      requestResponseHandlers, muon));
  }

  private void initDefaultRequestHandler() {
    this.requestResponseHandlers = new DynamicRequestResponseHandlers(new RequestResponseServerHandler() {

      @Override
      public HandlerPredicate getPredicate() {
        return HandlerPredicates.none();
      }

      @Override
      public void handle(RequestWrapper request) {
        request.notFound();
      }

      @Override
      public Map<String, SchemaDescriptor> getDescriptors() {
        return Collections.emptyMap();
      }
    });
  }

  @Override
  public Codecs getCodecs() {
    return muon.getCodecs();
  }

  @Override
  public RequestResponseHandlers getRequestResponseHandlers() {
    return requestResponseHandlers;
  }
}
