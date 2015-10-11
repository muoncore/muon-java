package io.muoncore.protocol.requestresponse.server;

import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.ServerProtocols;
import io.muoncore.protocol.channelfuture.ChannelFutureAdapter;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocol;
import io.muoncore.transport.TransportClientSource;
import org.reactivestreams.Publisher;

import java.util.function.Predicate;

public interface RequestResponseServerProtocolStack extends
        TransportClientSource {

    default <X, Y> Publisher<RequestWrapper<X,Y>> handleRequest(Predicate<Request<X>> request) {

        return null;
    }
}
