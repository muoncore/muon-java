package org.muoncore.transport;

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

    private synchronized MuonHttpHandler getHandler() {
        if (handler == null) {
            try {
                int port = new SecureRandom().nextInt(9000) + 2500;
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
    public MuonService.MuonResult emit(String eventName, MuonEvent event) {
        throw new IllegalStateException("HTTP Transport cannot broadcast/ emit");
    }

    @Override
    public MuonService.MuonResult emitForReturn(String eventName, MuonEvent event) {
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
                MuonEventBuilder builder = MuonEventBuilder.textMessage(exchange.getResponseContent());

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
    public void listenOnResource(String resource, String verb, Muon.EventTransportListener listener) {
        try {
            //TODO, need a global 'events' listener and enable chaining.
            System.out.println("HTTPTransport: Waiting for " + verb + " requests / " + resource);

            getHandler().addListener(resource, verb, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listenOnEvent(String event, Muon.EventTransportListener listener) {
        System.out.println("HTTP Transport cannot listen for broadcast");
    }

    @Override
    public List<ServiceDescriptor> discoverServices() {
        throw new IllegalStateException("Not Implemented");
    }

    public static class MuonHttpHandler extends AbstractHandler {
        Map<String, Muon.EventTransportListener> listeners = new HashMap<String, Muon.EventTransportListener>();

        public void addListener(String path, String verb, Muon.EventTransportListener listener) {
            //todo, blend in the verb too
            listeners.put(path, listener);
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            System.out.println("Getting target " + target);

            //TODO, need something that can give a response.
            Muon.EventTransportListener listener = listeners.get(target);

            if (listener != null) {
                response.setContentType("text/html;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);

                //TODO, read the content from the request.

                Object ret = listener.onEvent(target, "FAKE REQUEST");

//                String responseData = listener.onEvent(target, "WIBBLE").toString();
//                response.getWriter().println(responseData);

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
