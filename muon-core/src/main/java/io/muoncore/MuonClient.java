package io.muoncore;

import io.muoncore.future.MuonFuture;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.resource.MuonResourceEvent;

public interface MuonClient {

    public void emit(MuonMessageEvent event);
    public <T> MuonFuture<MuonResult<T>> query(MuonResourceEvent<T> payload, Class<T> type);
    public <T> MuonFuture<MuonResult<T>> command(String resource, MuonResourceEvent<T> payload, Class<T> type);

    public void shutdown();

    public static class MuonResult<T> {
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
