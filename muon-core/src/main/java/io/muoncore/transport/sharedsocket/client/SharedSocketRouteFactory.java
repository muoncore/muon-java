package io.muoncore.transport.sharedsocket.client;

public interface SharedSocketRouteFactory {
    SharedSocketRoute createRoute(String serviceName, Runnable onShutdown);
}
