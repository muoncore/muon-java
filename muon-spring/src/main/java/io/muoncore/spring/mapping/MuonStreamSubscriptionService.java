package io.muoncore.spring.mapping;

import io.muoncore.Muon;
import io.muoncore.spring.methodinvocation.MuonStreamMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MuonStreamSubscriptionService {

    List<StreamConnector> streamConnectors = new ArrayList<>();
    ScheduledExecutorService monitor = Executors.newScheduledThreadPool(1);

    @Autowired
    private Muon muon;

    @PostConstruct
    public void startMonitoring() {
        monitor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (StreamConnector streamConnector : streamConnectors) {
                    if (!streamConnector.isConnected()) {
                        try {
                            streamConnector.safeConnectToStream();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    public void setupMuonMapping(String streamUrl, final MuonStreamMethodInvocation muonStreamMethodInvocation) {
        StreamConnector streamConnector = new StreamConnector(muon, streamUrl, muonStreamMethodInvocation);
        try {
            streamConnector.safeConnectToStream();
        } catch (Exception e) {
            throw new MuonMappingException(e);
        }
        streamConnectors.add(streamConnector);
    }

}
