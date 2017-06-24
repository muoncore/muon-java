package io.muoncore.channel.impl;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.exception.MuonException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Dispatcher;

import java.util.concurrent.TimeUnit;

import static io.muoncore.transport.TransportEvents.CONNECTION_FAILURE;

/**
 * Implements keep alive behaviour.
 *
 * Assumes that there is another KeepAliveChannel down the pipe somewhere.
 *
 * If no data moves right within the keep alive period, sends a dedicated keep alive message
 *
 * If no data moves left within the keep alive period, will terminate the channel as failed.
 */
public class KeepAliveChannel implements Channel<MuonOutboundMessage, MuonInboundMessage> {

    public final static String KEEP_ALIVE_STEP = "keep-alive";

    private ChannelConnection<MuonOutboundMessage, MuonInboundMessage> left;
    private ChannelConnection<MuonInboundMessage, MuonOutboundMessage> right;

    private ChannelConnection.ChannelFunction<MuonOutboundMessage> leftFunction;
    private ChannelConnection.ChannelFunction<MuonInboundMessage> rightFunction;

    private static final long KEEP_ALIVE_TIMEOUT = 5500;
    private static final long KEEP_ALIVE_PERIOD = 1000;
    private final Scheduler scheduler;
    private Scheduler.TimerControl timeoutTimerControl;
    private Scheduler.TimerControl keepAliveTimerControl;
    private boolean channelFailed = false;
    private String protocol;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private long lastMsg;

    public KeepAliveChannel(Dispatcher dispatcher, String protocol, Scheduler scheduler) {
        this.protocol = protocol;
        this.scheduler = scheduler;
        String leftname = "left";
        String rightname = "right";

        resetKeepAlivePing();
        resetTimeout();

        left = new ChannelConnection<MuonOutboundMessage, MuonInboundMessage>() {
            @Override
            public void receive(ChannelFunction<MuonInboundMessage> function) {
                rightFunction = function;
            }

            @Override
            public void send(MuonOutboundMessage message) {
                if (message == null) {
                    shutdown();
                    return;
                }
                resetKeepAlivePing();
                if (leftFunction == null) {
                    throw new MuonException("Other side of the channel [" + rightname + "] is not connected to receive data");
                }
                dispatcher.dispatch(message, msg -> {
                    if (StandardAsyncChannel.echoOut) System.out.println("KeepAliveChannel[" + leftname + " >>>>> " + rightname + "]: Sending " + msg + " to " + leftFunction);
                    leftFunction.apply(message); }
                        ,  Throwable::printStackTrace);
                if (message.getChannelOperation() == MuonMessage.ChannelOperation.closed) {
                    shutdown();
                }
            }

            @Override
            public void shutdown() {
                timeoutTimerControl.cancel();
                keepAliveTimerControl.cancel();
                leftFunction.apply(null);
            }
        };

        right = new ChannelConnection<MuonInboundMessage, MuonOutboundMessage>() {
            @Override
            public void receive(ChannelFunction<MuonOutboundMessage> function) {
                leftFunction = function;
            }

            @Override
            public void send(MuonInboundMessage message) {
                if (rightFunction == null) {
                    throw new MuonException("Other side of the channel [" + rightname + "] is not connected to receive data");
                }
                if (message == null) {
                    rightFunction.apply(null);
                    return;
                }
                resetTimeout();
                if(!KEEP_ALIVE_STEP.equals(message.getStep())) {
                    dispatcher.dispatch(message, msg -> {
                        if (StandardAsyncChannel.echoOut)
                            System.out.println("KeepAliveChannel[" + leftname + " <<<< " + rightname + "]: " + msg + " to " + rightFunction);
                        rightFunction.apply(message);
                    }, Throwable::printStackTrace);
                }
                if (message == null || message.getChannelOperation() == MuonMessage.ChannelOperation.closed) {
                    shutdown();
                }
            }

            @Override
            public void shutdown() {
                timeoutTimerControl.cancel();
                keepAliveTimerControl.cancel();
            }
        };
    }

    @Override
    public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> right() {
        return right;
    }

    @Override
    public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> left() {
        return left;
    }

    private void resetKeepAlivePing() {
        if (channelFailed) return;
        if (keepAliveTimerControl != null) {
            keepAliveTimerControl.cancel();
        }
        keepAliveTimerControl = scheduler.executeIn(KEEP_ALIVE_PERIOD, TimeUnit.MILLISECONDS, () -> {
            left.send(MuonMessageBuilder.fromService("local")
                            .protocol(protocol)
                            .step(KEEP_ALIVE_STEP).build());
        });
    }

    private void resetTimeout() {
        lastMsg = System.currentTimeMillis();
        if (channelFailed) return;
        if (timeoutTimerControl != null) {
            timeoutTimerControl.cancel();
        }
        timeoutTimerControl = scheduler.executeIn(KEEP_ALIVE_TIMEOUT, TimeUnit.MILLISECONDS, () -> {
            keepAliveTimerControl.cancel();
            logger.debug("Connection has failed to stay alive, last message was received " + (System.currentTimeMillis() - this.lastMsg) + "ms ago, sending failure to protocol level: " + protocol);
            channelFailed = true;
            right().send(
                    MuonMessageBuilder.fromService("local")
                            .protocol(protocol)
                            .operation(MuonMessage.ChannelOperation.closed)
                            .step(CONNECTION_FAILURE).buildInbound());
            left().send(MuonMessageBuilder.fromService("local")
                    .protocol(protocol)
                    .operation(MuonMessage.ChannelOperation.closed)
                    .step(CONNECTION_FAILURE).build());
        });
    }
}
