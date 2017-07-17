package io.muoncore.transport.sharedsocket.client;


import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SharedSocketRouter {

  private final Map<String, SharedSocketRoute> routesToServices = new HashMap<>();
    private SharedSocketRouteFactory sharedSocketRouteFactory;

    public SharedSocketRouter(SharedSocketRouteFactory sharedSocketRouteFactory) {
        this.sharedSocketRouteFactory = sharedSocketRouteFactory;
    }

    public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> openClientChannel(String serviceName) throws NoSuchServiceException, MuonTransportFailureException {
        SharedSocketRoute sharedChannelRoute = getRoute(serviceName);
        return sharedChannelRoute.openClientChannel();
    }

    private synchronized SharedSocketRoute getRoute(String serviceName) {
        SharedSocketRoute route = routesToServices.get(serviceName);
        synchronized (routesToServices) {
          if (route == null) {
            log.debug("Creating a new shared-route for {}", serviceName);
            route = sharedSocketRouteFactory.createRoute(serviceName, () -> {
              log.debug("Removing shared-route to service {}", serviceName);
              synchronized (routesToServices) {
                routesToServices.remove(serviceName);
              }
            });
            routesToServices.put(serviceName, route);
          }
        }
        return route;
    }
}
