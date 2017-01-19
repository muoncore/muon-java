package io.muoncore.spring.mapping;

import io.muoncore.Muon;
import io.muoncore.spring.controllers.MuonControllersConfigurationHolder;
import io.muoncore.spring.methodinvocation.MuonStreamMethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MuonStreamSubscriptionService {

    List<StreamConnector> streamConnectors = new ArrayList<>();
    ScheduledExecutorService monitor = Executors.newScheduledThreadPool(1);

    @Autowired
    private Muon muon;

    @Autowired
    private MuonControllersConfigurationHolder muonControllersConfigurationHolder;

    private static Logger LOG = LoggerFactory.getLogger(MuonStreamSubscriptionService.class.getName());

    @PostConstruct
    public void startMonitoring() {
        monitor.scheduleAtFixedRate(() -> {
                    LOG.debug("Checking connections");
            for (StreamConnector streamConnector : streamConnectors) {
                if (!streamConnector.isConnected()) {
                    try {
                        LOG.info("Trying to reconnect to " + streamConnector.getMuonUrl());
                        streamConnector.safeConnectToStream();
                    } catch (URISyntaxException | UnsupportedEncodingException e) {
                        e.printStackTrace();
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
