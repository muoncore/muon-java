package io.muoncore.channel.impl;

import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Dispatcher;
import io.muoncore.exception.MuonException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Zips/ unzips up the messages moving along the channel
 */
public class ZipChannel implements Channel<MuonOutboundMessage, MuonInboundMessage> {

    private ChannelConnection<MuonOutboundMessage, MuonInboundMessage> left;
    private ChannelConnection<MuonInboundMessage, MuonOutboundMessage> right;

    private ChannelConnection.ChannelFunction<MuonOutboundMessage> leftFunction;
    private ChannelConnection.ChannelFunction<MuonInboundMessage> rightFunction;

    private static Logger logger = LoggerFactory.getLogger(ZipChannel.class);

    public ZipChannel(Dispatcher dispatcher, String name) {
        String leftname = name + "left";
        String rightname = name + "right";

        left = new ChannelConnection<MuonOutboundMessage, MuonInboundMessage>() {
            @Override
            public void receive(ChannelFunction<MuonInboundMessage> function) {
                rightFunction = function;
            }

            @Override
            public void send(MuonOutboundMessage message) {
                if (leftFunction == null) {
                    throw new MuonException("Other side of the channel [" + rightname + "] is not connected to receive data");
                }

                dispatcher.dispatch(message, msg -> {
                    if (StandardAsyncChannel.echoOut) System.out.println("ZipChannel[" + leftname + " >>>>> " + rightname + "]: Sending " + msg + " to " + leftFunction);
                    leftFunction.apply(zipOutbound(message)); }
                        ,  Throwable::printStackTrace);
            }

            @Override
            public void shutdown() {
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
                        System.out.println("ZipChannel[" + leftname + " <<<< " + rightname + "]: Receiving " + msg + " to " + rightFunction);
                    rightFunction.apply(zipInbound(message));
                }, Throwable::printStackTrace);
            }

            @Override
            public void shutdown() {
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

    private MuonInboundMessage zipInbound(MuonInboundMessage msg) {
        if (msg == null) return null;
        if (msg.getContentType() == null) {
            return msg;
        }

        if (msg.getContentType().indexOf("DEFLATE") <= 0) {
            return msg;
        }
        return MuonMessageBuilder
                .clone(msg)
                .payload(zlibInflate(msg.getPayload()))
                .contentType(msg.getContentType().substring(0, msg.getContentType().indexOf("+DEFLATE")))
                .buildInbound();
    }
    private MuonOutboundMessage zipOutbound(MuonOutboundMessage msg) {
        if (msg == null) return null;
        return MuonMessageBuilder
                .clone(msg)
                .payload(zlibDeflate(msg.getPayload()))
                .contentType(msg.getContentType() + "+DEFLATE")
                .build();
    }

    public static byte[] zlibDeflate(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) return bytes;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(os);
        try {
            dos.write(bytes);
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return os.toByteArray();
    }

    public static byte[] zlibInflate(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) return bytes;
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final InflaterInputStream iis = new InflaterInputStream(bais);
        final byte[] buf = new byte[1024];

        try {
            int count = iis.read(buf);
            while (count != -1) {
                baos.write(buf, 0, count);
                count = iis.read(buf);
            }
            iis.close();
            baos.close();
            return baos.toByteArray();
        } catch (final Exception e) {
          logger.warn("Unable to inflate zipped payload", e);
            return null;
        }
    }
}
