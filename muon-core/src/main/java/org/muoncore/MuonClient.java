package org.muoncore;

import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.transports.MuonResourceEvent;

public interface MuonClient {

    public void emit(MuonMessageEvent event);
    public void sendMessage(MuonMessageEvent event);
    public <T> MuonResult<T> get(String resourceQuery, Class<T> type);
    public <T> MuonResult<T> post(String resource, MuonResourceEvent<T> payload, Class<T> type);
    public <T> MuonResult<T> put(String resource, MuonResourceEvent<T> payload, Class<T> type);

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
