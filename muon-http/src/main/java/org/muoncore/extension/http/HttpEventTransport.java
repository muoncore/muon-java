package org.muoncore.extension.http;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.muoncore.*;
import org.muoncore.codec.TransportCodecType;
import org.muoncore.transports.MuonResourceEvent;
import org.muoncore.transports.MuonResourceEventBuilder;
import org.muoncore.transports.MuonResourceTransport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HttpEventTransport implements MuonResourceTransport {

    private Logger log = Logger.getLogger(HttpEventTransport.class.getName());
    private Server server;
    private MuonHttpHandler handler;
    private int port;
    private HttpTransportServiceDiscovery transportServiceDiscovery;

    public HttpEventTransport(int port) {
        this.port = port;
    }

    private synchronized MuonHttpHandler getHandler() {
        if (handler == null) {
            try {
                log.fine("HTTP Transport: Booting local HTTP server on port " + port);
                server = new Server(port);
                handler = new MuonHttpHandler();

                server.setHandler(handler);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return handler;
    }

    @Override
    public MuonService.MuonResult emitForReturn(String eventName, MuonResourceEvent event) {
        try {
            //TODO, lookup/ derive the target server from the onGet/ presend
            String remoteHost = "localhost";
            int remotePort = 8080;

            if (!eventName.startsWith("/")) {
                eventName = "/" + eventName;
            }

            String url = "http://" + remoteHost + ":" + remotePort + eventName;

            HttpClient client = new HttpClient();
            client.start();

            ContentExchange exchange = new ContentExchange(true);
            exchange.setURL(url);

            client.send(exchange);

            int exchangeState = exchange.waitForDone();

            if (exchangeState == HttpExchange.STATUS_COMPLETED) {
                MuonResourceEventBuilder builder = MuonResourceEventBuilder.event(exchange.getResponseContent());

                for(String headerName: Collections.list(exchange.getResponseFields().getFieldNames())) {
                    builder.withHeader(headerName, exchange.getResponseFields().getStringField(headerName));
                }

                MuonService.MuonResult result = new MuonService.MuonResult();
                result.setEvent(builder.build());

                return result;
            } else if (exchangeState == HttpExchange.STATUS_EXCEPTED) {
                log.info("STATUS_EXCEPTED");
//                handleError();
            } else if (exchangeState == HttpExchange.STATUS_EXPIRED) {
                log.info("STATUS_EXPIRED");
//                handleSlowServer();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MuonService.MuonResult();
    }

    @Override
    public <T> void listenOnResource(String resource, String verb, Class<T> type, Muon.EventResourceTransportListener<T> listener) {
        try {
            log.info("HTTPTransport: Waiting for " + verb + " requests / " + resource);
            getHandler().addListener(resource, verb.toUpperCase(), listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            handler.stop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            transportServiceDiscovery.unregister();
        }
    }

    public void start() throws Exception {
        getHandler();
        server.start();
        //transportServiceDiscovery.register();
    }

    public static class MuonHttpHandler extends AbstractHandler {
        Map<String, Muon.EventResourceTransportListener> listeners = new HashMap<String, Muon.EventResourceTransportListener>();

        private Logger log = Logger.getLogger(HttpEventTransport.class.getName());

        public void addListener(String path, String verb, Muon.EventResourceTransportListener listener) {
            listeners.put(verb + " " + path, listener);
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            log.fine("Getting target " + target);

            String lookup = baseRequest.getMethod().toUpperCase() + " " + target;

            Muon.EventResourceTransportListener listener = listeners.get(lookup);

            if (listener != null) {
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

                MuonResourceEvent ev;
                if (request.getContentLength() > 0) {
                    //this is a bit rubbish, pull in a proper lib to do this.
                    byte[] content = new byte[request.getContentLength()];
                    request.getInputStream().read(content);

                    ev = MuonResourceEventBuilder.event(new String(content))
//                            .withMimeType(request.getContentType())
                            .build();
                } else {
                    ev = MuonResourceEventBuilder.event("")
//                            .withMimeType(request.getContentType())
                            .build();
                }

                Object ret = listener.onEvent(target, ev);

                response.getWriter().println(ret.toString());
            } else {
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                baseRequest.setHandled(true);
                response.getWriter().println("<h1>Hello World</h1>");
            }
        }
    }

    @Override
    public String getUrlScheme() {
        return "http";
    }

    @Override
    public URI getLocalConnectionURI() throws URISyntaxException {
        return new URI("http://localhost:" + port);
    }

    @Override
    public TransportCodecType getCodecType() {
        return TransportCodecType.TEXT;
    }
}
