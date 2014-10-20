package org.muoncore.extension.http;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.muoncore.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpEventTransport implements MuonEventTransport {

    private Server server;
    private MuonHttpHandler handler;
    private int port;

    public HttpEventTransport(int port) {
        this.port = port;
    }

    private synchronized MuonHttpHandler getHandler() {
        if (handler == null) {
            try {
                System.out.println("HTTP Transport: Booting local HTTP server on port " + port);
                server = new Server(port);
                handler = new MuonHttpHandler();

                server.setHandler(handler);

                server.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return handler;
    }

    @Override
    public MuonService.MuonResult emit(String eventName, MuonBroadcastEvent event) {
        throw new IllegalStateException("HTTP Transport cannot broadcast/ emit");
    }

    @Override
    public MuonService.MuonResult emitForReturn(String eventName, MuonResourceEvent event) {
        try {
            //TODO, lookup/ derive the target server from the resource/ presend
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
                MuonResourceEventBuilder builder = MuonResourceEventBuilder.textMessage(exchange.getResponseContent());

                for(String headerName: Collections.list(exchange.getResponseFields().getFieldNames())) {
                    builder.withHeader(headerName, exchange.getResponseFields().getStringField(headerName));
                }

                MuonService.MuonResult result = new MuonService.MuonResult();
                result.setEvent(builder.build());

                return result;
            } else if (exchangeState == HttpExchange.STATUS_EXCEPTED) {
                System.out.println("STATUS_EXCEPTED");
//                handleError();
            } else if (exchangeState == HttpExchange.STATUS_EXPIRED) {
                System.out.println("STATUS_EXPIRED");
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
    public void listenOnResource(String resource, String verb, Muon.EventResourceTransportListener listener) {
        try {
            //TODO, need a global 'events' listener and enable chaining.
            System.out.println("HTTPTransport: Waiting for " + verb + " requests / " + resource);

            getHandler().addListener(resource, verb.toUpperCase(), listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listenOnEvent(String event, Muon.EventBroadcastTransportListener listener) {
        System.out.println("HTTP Transport cannot listen for broadcast");
    }

    @Override
    public List<ServiceDescriptor> discoverServices() {
        throw new IllegalStateException("Not Implemented");
    }

    @Override
    public void shutdown() {
        try {
            handler.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        //TODO ...
    }

    public static class MuonHttpHandler extends AbstractHandler {
        Map<String, Muon.EventResourceTransportListener> listeners = new HashMap<String, Muon.EventResourceTransportListener>();

        public void addListener(String path, String verb, Muon.EventResourceTransportListener listener) {
            listeners.put(verb + " " + path, listener);
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            System.out.println("Getting target " + target);

            String lookup = baseRequest.getMethod().toUpperCase() + " " + target;

            Muon.EventResourceTransportListener listener = listeners.get(lookup);

            if (listener != null) {
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

                //this is a bit rubbish, pull in a proper lib to do this.
                byte[] content = new byte[request.getContentLength()];
                request.getInputStream().read(content);

                //TODO, read the content from the request.
                MuonResourceEvent ev = MuonResourceEventBuilder.textMessage(new String(content))
                        .withMimeType(request.getContentType())
                        .build();

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
}
