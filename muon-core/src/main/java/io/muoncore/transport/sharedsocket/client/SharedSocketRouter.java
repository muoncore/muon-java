package io.muoncore.transport.sharedsocket.client;


import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

import java.util.HashMap;
import java.util.Map;

public class SharedSocketRouter {

    public static final String PROTOCOL = "shared-channel";

    private Map<String, SharedSocketRoute> routesToServices = new HashMap<>();
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
        if (route == null) {
            route = sharedSocketRouteFactory.createRoute(serviceName,() -> {
              routesToServices.remove(serviceName);
            });
            routesToServices.put(serviceName, route);
        }
        return route;
    }
}
