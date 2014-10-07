package org.muoncore.transport;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.muoncore.MuonEvent;
import org.muoncore.TransportedMuon;
import org.muoncore.Muon;
import org.muoncore.MuonEventTransport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpEventTransport implements MuonEventTransport {

    private Server server;
    private MuonHttpHandler handler;

    public HttpEventTransport() throws Exception {
        try {
            server = new Server(8080);
            handler = new MuonHttpHandler();

            server.setHandler(handler);

            server.start();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Muon.MuonResult emit(String eventName, MuonEvent event) {
        throw new IllegalStateException("HTTP Transport cannot broadcast/ emit");
    }

    @Override
    public Muon.MuonResult emitForReturn(String eventName, MuonEvent event) {
        try {
            //TODO, lookup/ derive the target server from the resource/ event
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
                Muon.MuonResult result = new Muon.MuonResult();

                result.setEvent(exchange.getResponseContent());

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
        return new Muon.MuonResult();
    }

    @Override
    public void listenOnResource(String resource, String verb, TransportedMuon.EventTransportListener listener) {
        try {
            //TODO, need a global 'events' listener and enable chaining.
            System.out.println("HTTPTransport: Waiting for " + verb + " requests / " + resource);

            handler.addListener(resource, verb, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listenOnEvent(String event, TransportedMuon.EventTransportListener listener) {
        System.out.println("HTTP Transport cannot listen for broadcast");
    }

    public static class MuonHttpHandler extends AbstractHandler {
        Map<String, TransportedMuon.EventTransportListener> listeners = new HashMap<String, TransportedMuon.EventTransportListener>();

        public void addListener(String path, String verb, TransportedMuon.EventTransportListener listener) {
            //todo, blend in the verb too
            listeners.put(path, listener);
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            System.out.println("Getting target " + target);

            //TODO, need something that can give a response.
            TransportedMuon.EventTransportListener listener = listeners.get(target);

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
