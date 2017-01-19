package io.muoncore.spring.teststore;

import io.muoncore.MultiTransportMuon;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.server.EventServerProtocolStack;
import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.MuonController;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Implementation of a muon event store that is simplistic to the point of being fairly broken.
 * Needs a threading overhaul.
 * Useful for simple local testing, use photon for anything else, which has correct threading semantics.
 */
@SpringBootApplication
@MuonController
@EnableMuon(serviceName = "chronos", tags = {"eventstore"})
public class EventStoreInMem {

    @Autowired public MultiTransportMuon muon;
    private List<Event> history = new ArrayList<>();
    private List<SubQueue> subs = new ArrayList<>();
    private final Executor exec = Executors.newFixedThreadPool(10);

    @PostConstruct
    public void setupEventStore() {

        exec.execute(() -> {
            try {
                while(true) {
                    synchronized (exec) {
                        exec.wait(500);
                        //drain all the queues
                        subs.stream().forEach(sq -> {
                            synchronized (sq.queue) {
                                while(sq.queue.size() > 0) {
                                    sq.sub.onNext(sq.queue.poll());
                                }
                            }
                        });
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        muon.getProtocolStacks().registerServerProtocol(new EventServerProtocolStack(event -> {
            try {
                subs.stream().forEach(q -> {
                    synchronized (q.queue) {
                        q.queue.add(event.getEvent());
                    }
                });
                synchronized (history) {
                    history.add(event.getEvent());
                }
                synchronized (exec) {
                    exec.notifyAll();
                }
                event.persisted(12313, System.currentTimeMillis());
            } catch (Exception ex) {
                event.failed(ex.getMessage());
            }
        }, muon.getCodecs(), muon.getDiscovery()));

        muon.publishGeneratedSource("general", PublisherLookup.PublisherType.HOT_COLD, subscriptionRequest -> {
            return subscriber -> {
                Queue<Event> queue = new LinkedList<>();
                synchronized (history) {
                    history.stream().forEach(queue::add);
                }
                subs.add(new SubQueue(queue, subscriber));
            };
        });
    }

    static class SubQueue {
        Queue<Event> queue;
        Subscriber<Event> sub;

        public SubQueue(Queue queue, Subscriber sub) {
            this.queue = queue;
            this.sub = sub;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(EventStoreInMem.class);
    }
}
