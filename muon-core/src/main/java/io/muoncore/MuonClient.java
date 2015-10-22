package io.muoncore;

import io.muoncore.future.MuonFuture;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.resource.MuonResourceEvent;
import org.reactivestreams.Subscriber;

import java.net.URISyntaxException;
import java.util.Map;

public interface MuonClient {

    <T> MuonFuture<MuonResult<T>> query(MuonResourceEvent<T> payload, Class<T> type);
    <T> MuonFuture<MuonResult<T>> command(String resource, MuonResourceEvent<T> payload, Class<T> type);
    <T> void subscribe(String url, Class<T> type, Subscriber<T> subscriber) throws URISyntaxException;
    <T> void subscribe(String url, Class<T> type, Map<String, String> params, Subscriber<T> subscriber) throws URISyntaxException;

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
}
