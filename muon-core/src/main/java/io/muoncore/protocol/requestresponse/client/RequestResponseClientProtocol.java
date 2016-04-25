package io.muoncore.protocol.requestresponse.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.requestresponse.RRPEvents;
import io.muoncore.protocol.requestresponse.RRPTransformers;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.support.ProtocolTimer;
import io.muoncore.transport.TransportEvents;

/**
 * Request Response client middleware protocol.
 * <p>
 * Add reliability, timeout etc.
 */
public class RequestResponseClientProtocol {

    private Codecs codecs;
    private ProtocolTimer timer;

    private ProtocolTimer.TimerControl localTimeoutEvent;

    public RequestResponseClientProtocol(
            String serviceName,
            final ChannelConnection<Response, Request> leftChannelConnection,
            final ChannelConnection<MuonOutboundMessage, MuonInboundMessage> rightChannelConnection,
            final Codecs codecs,
            final ProtocolTimer timer) {

        rightChannelConnection.receive(message -> {
            if (message == null) {
                leftChannelConnection.shutdown();
                return;
            }

            switch (message.getStep()) {
                case RRPEvents.RESPONSE:
                    leftChannelConnection.send(
                            RRPTransformers.toResponse(message, codecs));
                    break;
                case RRPEvents.RESPONSE_FAILED:
                    leftChannelConnection.send(
                            RRPTransformers.toResponse(message, codecs));
                    break;
                case TransportEvents.SERVICE_NOT_FOUND:
                    Codecs.EncodingResult encoded = codecs.encode("No such service " + message.getSourceServiceName(), codecs.getAvailableCodecs());
                    leftChannelConnection.send(
                            new Response(
                                    404,
                                    encoded.getPayload(),
                                    encoded.getContentType(),
                                    codecs));
                    break;
                default:
                    Codecs.EncodingResult encoded500 = codecs.encode("Unknown error sending to " + message.getSourceServiceName(), codecs.getAvailableCodecs());
                    leftChannelConnection.send(
                            new Response(
                                    500,
                                    encoded500.getPayload(),
                                    encoded500.getContentType(),
                                    codecs));
            }
        });

        leftChannelConnection.receive(request -> {
            if (request == null) {
                rightChannelConnection.shutdown();
                return;
            }
            rightChannelConnection.send(RRPTransformers.toOutbound(
                    serviceName,
                    request, codecs, codecs.getAvailableCodecs()));
        });

        /**
         * handle 404.
         * handle local timeout.
         *
         *
         */
    }

}
