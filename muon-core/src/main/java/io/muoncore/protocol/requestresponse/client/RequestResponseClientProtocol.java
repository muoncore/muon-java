package io.muoncore.protocol.requestresponse.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.protocol.requestresponse.RRPEvents;
import io.muoncore.protocol.requestresponse.RRPTransformers;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.protocol.support.ProtocolTimer;
import io.muoncore.transport.TransportEvents;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

import java.lang.reflect.Type;

/**
 * Request Response client middleware protocol.
 *
 * Add reliability, timeout etc.
 *
 */
public class RequestResponseClientProtocol<X,R> {

    private Codecs codecs;
    private ProtocolTimer timer;

    private ProtocolTimer.TimerControl localTimeoutEvent;

    public RequestResponseClientProtocol(
            String serviceName,
            final ChannelConnection<Response<R>, Request<X>> leftChannelConnection,
            final ChannelConnection<MuonOutboundMessage, MuonInboundMessage> rightChannelConnection,
            final Type responseType,
            final Codecs codecs,
            final ProtocolTimer timer) {

        rightChannelConnection.receive( message -> {
            if (message == null) {
               leftChannelConnection.shutdown();
                return;
            }

            switch(message.getStep()) {
                case RRPEvents.RESPONSE:
                    leftChannelConnection.send(
                            RRPTransformers.toResponse(message, codecs, responseType));
                    break;
                case TransportEvents.SERVICE_NOT_FOUND:
                    leftChannelConnection.send(
                            new Response<>(
                            404,
                            null));
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
