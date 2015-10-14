package io.muoncore;

import io.muoncore.protocol.DynamicRegistrationServerStacks;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.protocol.defaultproto.DefaultServerProtocol;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.requestresponse.server.DynamicRequestResponseHandlers;
import io.muoncore.protocol.requestresponse.server.RequestResponseHandlers;
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandler;
import io.muoncore.protocol.requestresponse.server.RequestWrapper;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.client.SingleTransportClient;
import io.muoncore.transport.client.TransportClient;

import java.util.function.Predicate;

/**
 * Simple bundle of default Muon protocol stacks based on a single transport.
 */
public class SingleTransportMuon implements Muon
{

    private TransportClient transportClient;
    private Discovery discovery;
    private ServerStacks protocols;
    private RequestResponseHandlers requestResponseHandlers;

    public SingleTransportMuon(
            Discovery discovery,
            MuonTransport transport) {
        this.transportClient = new SingleTransportClient(transport);
        this.discovery = discovery;
        this.protocols = new DynamicRegistrationServerStacks(new DefaultServerProtocol());
        this.requestResponseHandlers = new DynamicRequestResponseHandlers(new RequestResponseServerHandler() {
            @Override
            public Predicate<Request> getPredicate() {
                return request -> false;
            }

            @Override
            public void handle(RequestWrapper request) {
                request.answer(new Response(request.getRequest().getUrl()));
            }
        });
    }

    @Override
    public Discovery getDiscovery() {
        return discovery;
    }

    @Override
    public RequestResponseHandlers getRequestResponseHandlers() {
        return requestResponseHandlers;
    }

    //
//    @Override
//    public AutoConfiguration getConfiguration() {
//        return null;
//    }


    @Override
    public TransportClient getTransportClient() {
        return transportClient;
    }

    public static void main(String[] args) {
        SingleTransportMuon muon = new SingleTransportMuon(null, null);

        muon.handleRequest( request -> true, requestWrapper -> {
            requestWrapper.answer(new Response(""));
        });
    }
}
