package io.muoncore;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.protocol.DynamicRegistrationServerStacks;
import io.muoncore.protocol.ServerRegistrar;
import io.muoncore.protocol.ServerStacks;
import io.muoncore.protocol.defaultproto.DefaultServerProtocol;
import io.muoncore.protocol.requestresponse.RRPTransformers;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.RequestMetaData;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.requestresponse.server.*;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.client.SingleTransportClient;
import io.muoncore.transport.client.TransportClient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Simple bundle of default Muon protocol stacks based on a single transport.
 */
public class SingleTransportMuon implements Muon
{

    private TransportClient transportClient;
    private Discovery discovery;
    private ServerStacks protocols;
    private ServerRegistrar registrar;
    private RequestResponseHandlers requestResponseHandlers;
    private Codecs codecs;
    private AutoConfiguration configuration;

    public SingleTransportMuon(
            AutoConfiguration configuration,
            Discovery discovery,
            MuonTransport transport) {
        this.configuration = configuration;
        this.transportClient = new SingleTransportClient(transport);
        this.discovery = discovery;
        DynamicRegistrationServerStacks stacks = new DynamicRegistrationServerStacks(new DefaultServerProtocol());
        this.protocols = stacks;
        this.registrar = stacks;
        this.codecs = new JsonOnlyCodecs();

        initServerStacks(stacks);
        initDefaultRequestHandler();
    }

    private void initServerStacks(DynamicRegistrationServerStacks stacks) {
        stacks.registerServerProtocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                new RequestResponseServerProtocolStack(
                        requestResponseHandlers, codecs));
    }

    private void initDefaultRequestHandler() {
        this.requestResponseHandlers = new DynamicRequestResponseHandlers(new RequestResponseServerHandler<Map, Map>() {
            @Override
            public Predicate<RequestMetaData> getPredicate() {
                return request -> false;
            }

            @Override
            public void handle(RequestWrapper<Map, Map> request) {
                request.answer(new Response<>(404, new HashMap<>()));
            }

            @Override
            public Class<Map> getRequestType() {
                return Map.class;
            }
        });
    }

    @Override
    public Codecs getCodecs() {
        return codecs;
    }

    @Override
    public Discovery getDiscovery() {
        return discovery;
    }

    @Override
    public RequestResponseHandlers getRequestResponseHandlers() {
        return requestResponseHandlers;
    }

    @Override
    public TransportClient getTransportClient() {
        return transportClient;
    }

    @Override
    public AutoConfiguration getConfiguration() {
        return configuration;
    }

    //
//    public static void main(String[] args) {
//        SingleTransportMuon muon = new SingleTransportMuon(discover, amqpTransport);
//
//        muon.handleRequest(
//                request -> request.getUrl().startsWith("/hello"),
//                requestWrapper -> {
//            requestWrapper.answer(new Response(requestWrapper.getRequest().getId(), "hello"));
//        });
//    }
}
