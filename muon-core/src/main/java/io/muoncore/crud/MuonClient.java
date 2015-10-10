package io.muoncore.crud;

import io.muoncore.exception.MuonException;
import io.muoncore.transport.crud.requestresponse.MuonResourceEvent;
import org.reactivestreams.Subscriber;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

public interface MuonClient {
//
//    <T> MuonFuture<MuonResult<T>> query(MuonQuery<T> query, Class<T> returnType);
//    <T> MuonFuture<MuonResult<T>> command(MuonCommand<T> command, Class<T> returnType);

    <T> void subscribe(String url, Class<T> type, Subscriber<MuonEvent<T>> subscriber) throws URISyntaxException;
    <T> void subscribe(String url, Class<T> type, Map<String, String> params, Subscriber<MuonEvent<T>> subscriber) throws URISyntaxException;

    <T> void event(MuonEvent<T> event) throws MuonException;
    MuonEventChain lookupEventChain(String id) throws MuonException;

    void shutdown();

    class MuonResult<T> {
        private boolean success;

        private MuonResourceEvent<T> event;

        public MuonResourceEvent<T> getResponseEvent() {
            return event;
        }

        public void setEvent(MuonResourceEvent<T> event) {
            this.event = event;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }

    class MuonEvent<T> {
        private String action;
        private String id;
        private String parentId;
        private String serviceId;

        private T payload;
    }

    interface MuonEventChain {
        MuonEventChainNode getRoot();
        MuonEventChainNode lookupById(String id);
    }

    class MuonEventChainNode {
        private MuonEvent event;
        private MuonEventChainNode parent;
        private Set<MuonEventChainNode> children;
    }
}
