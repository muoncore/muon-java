package io.muoncore.channel.impl;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.exception.MuonException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import reactor.core.Dispatcher;

import java.util.concurrent.TimeUnit;

public class TimeoutChannel implements Channel<MuonOutboundMessage, MuonInboundMessage> {

    public final static String TIMEOUT_STEP = "TIMEOUT";

    private ChannelConnection<MuonOutboundMessage, MuonInboundMessage> left;
    private ChannelConnection<MuonInboundMessage, MuonOutboundMessage> right;

    private ChannelConnection.ChannelFunction<MuonOutboundMessage> leftFunction;
    private ChannelConnection.ChannelFunction<MuonInboundMessage> rightFunction;

    private final long timeout;
    private final Scheduler scheduler;
    private Scheduler.TimerControl timerControl;

    public TimeoutChannel(Dispatcher dispatcher, Scheduler scheduler, long timeout) {
        this.timeout = timeout;
        this.scheduler = scheduler;
        String leftname = "left";
        String rightname = "right";

        left = new ChannelConnection<MuonOutboundMessage, MuonInboundMessage>() {
            @Override
            public void receive(ChannelFunction<MuonInboundMessage> function) {
                rightFunction = function;
            }

            @Override
            public void send(MuonOutboundMessage message) {
                resetTimeout();
                if (leftFunction == null) {
                    throw new MuonException("Other side of the channel [" + rightname + "] is not connected to receive data");
                }
                dispatcher.dispatch(message, msg -> {
                    if (StandardAsyncChannel.echoOut) System.out.println("WiretapChannel[" + leftname + " >>>>> " + rightname + "]: Sending " + msg + " to " + leftFunction);
                    leftFunction.apply(message); }
                        ,  Throwable::printStackTrace);
            }

            @Override
            public void shutdown() {
              if (timerControl != null) timerControl.cancel();
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
                dispatcher.dispatch(message, msg -> {
                    if (StandardAsyncChannel.echoOut)
                        System.out.println("WiretapChannel[" + leftname + " <<<< " + rightname + "]: " + msg + " to " + rightFunction);
                    rightFunction.apply(message);
                }, Throwable::printStackTrace);
            }

            @Override
            public void shutdown() {
                timerControl.cancel();
                rightFunction.apply(null);
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

    private void resetTimeout() {
        if (timerControl != null) {
            timerControl.cancel();
        }
        timerControl = scheduler.executeIn(timeout, TimeUnit.MILLISECONDS, () -> {
            rightFunction.apply(
                    MuonMessageBuilder.fromService("local")
                    .protocol("timeout")
                    .step(TIMEOUT_STEP).buildInbound());
        });
    }
}
