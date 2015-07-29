package io.muoncore.spring.annotationprocessor;

import io.muoncore.spring.annotations.PhotonStreamListener;
import io.muoncore.spring.mapping.MuonStreamSubscriptionService;
import io.muoncore.spring.methodinvocation.MuonStreamMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PhotonStreamListenerMethodAnnotationProcessor implements MethodAnnotationProcessor {
    @Autowired
    private MuonStreamSubscriptionService streamSubscriptionService;

    @Override
    public void processMethod(Method method, Object bean) {
        PhotonStreamListener photonStreamListener = AnnotationUtils.findAnnotation(method, PhotonStreamListener.class);
        if (photonStreamListener != null) {
            streamSubscriptionService.setupMuonMapping(
                    photonStreamListener.photonUrl(),
                    getPhotonParams(photonStreamListener),
                    new MuonStreamMethodInvocation(method, bean));
        }
    }

    private Map<String, String> getPhotonParams(PhotonStreamListener photonStreamListener) {
        HashMap<String, String> params = new HashMap<>();
        params.put("stream-name", photonStreamListener.streamName());
        params.put("from", String.valueOf(photonStreamListener.from()));
        params.put("stream-type", photonStreamListener.streamType().getName());
        return params;
    }

    void setStreamSubscriptionService(MuonStreamSubscriptionService streamSubscriptionService) {
        this.streamSubscriptionService = streamSubscriptionService;
    }
}
