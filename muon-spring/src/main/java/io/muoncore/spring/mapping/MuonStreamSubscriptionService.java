package io.muoncore.spring.mapping;

import io.muoncore.crud.OldMuon;
import io.muoncore.spring.controllers.MuonControllersConfigurationHolder;
import io.muoncore.spring.methodinvocation.MuonStreamMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MuonStreamSubscriptionService {

    List<StreamConnector> streamConnectors = new ArrayList<>();
    ScheduledExecutorService monitor = Executors.newScheduledThreadPool(1);

    @Autowired
    private OldMuon muon;

    @Autowired
    private MuonControllersConfigurationHolder muonControllersConfigurationHolder;

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
        }, muonControllersConfigurationHolder.getStreamKeepAliveTimeout(),
                muonControllersConfigurationHolder.getStreamKeepAliveTimeout(),
                muonControllersConfigurationHolder.getTimeUnit());
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
