package io.muoncore.protocol.rpc.server;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.CodecsSource;
import io.muoncore.descriptors.SchemaDescriptor;

import java.util.*;

public interface RequestResponseServerHandlerApi extends
  RequestResponseHandlersSource, CodecsSource {

  /**
   * RPC handler API. Each incoming request will be passed to the handler instance for it
   * to reply to.
   * <p>
   * The predicate is used to match requests.
   */
  default void handleRequest(
    final HandlerPredicate predicate,
    final Handler handler) {
    getRequestResponseHandlers().addHandler(new RequestResponseServerHandler() {
      @Override
      public HandlerPredicate getPredicate() {
        return predicate;
      }

      @Override
      public void handle(RequestWrapper request) {
        handler.handle(request);
      }

      @Override
      public Map<String, SchemaDescriptor> getDescriptors() {
        System.out.println("EMPTY DESCRIPTORS FOR " + getPredicate().resourceString());

        return Collections.emptyMap();
      }
    });
  }

  default HandlerBuilder handleRequest(HandlerPredicate predicate) {
    return HandlerBuilder.buildHandler(getCodecs(), getRequestResponseHandlers(), predicate);
  }

  interface Handler {
    void handle(RequestWrapper wrapper);
  }

  class HandlerBuilder {
    private Codecs codecs;
    private RequestResponseHandlers handlers;
    private HandlerPredicate predicate;

    private Handler thehandler;
    private List<Class> requestTypes = new ArrayList<>();
    private List<Class> responseTypes = new ArrayList<>();

    public static HandlerBuilder buildHandler(Codecs codecs, RequestResponseHandlers handlers, HandlerPredicate predicate) {
      HandlerBuilder handlerBuilder = new HandlerBuilder();
      handlerBuilder.codecs = codecs;
      handlerBuilder.handlers = handlers;
      handlerBuilder.predicate = predicate;
      return handlerBuilder;
    }

    public HandlerBuilder addRequestType(Class type) {
      requestTypes.add(type);
      return this;
    }
    public HandlerBuilder addResponseType(Class type) {
      responseTypes.add(type);
      return this;
    }
    public HandlerBuilder handler(Handler handler) {
      this.thehandler = handler;
      return this;
    }
    public void build() {
      handlers.addHandler(new RequestResponseServerHandler() {
        @Override
        public HandlerPredicate getPredicate() {
          return predicate;
        }

        @Override
        public void handle(RequestWrapper request) {
          //TODO, use the schemas?
          thehandler.handle(request);
        }

        @Override
        public Map<String, SchemaDescriptor> getDescriptors() {
          Map<String, SchemaDescriptor> schemas = new HashMap<>();

          for (int i = 0; i < requestTypes.size(); i++) {
            String schemaName = "Request-" + requestTypes.get(i).getSimpleName();
            codecs.getSchemaFor(requestTypes.get(i)).ifPresent(schemaInfo -> {
              schemas.put(schemaName, new SchemaDescriptor(schemaName, schemaInfo.getSchemaText(), schemaInfo.getSchemaType()));
            });
          }
          for (int i = 0; i < requestTypes.size(); i++) {
            String schemaName = "Response-" + responseTypes.get(i).getSimpleName();
            codecs.getSchemaFor(responseTypes.get(i)).ifPresent(schemaInfo -> {
              schemas.put(schemaName, new SchemaDescriptor(schemaName, schemaInfo.getSchemaText(), schemaInfo.getSchemaType()));
            });
          }

          return schemas;
        }
      });
    }
  }
}
