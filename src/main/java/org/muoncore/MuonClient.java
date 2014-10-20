package org.muoncore;

public interface MuonClient {

    public void emit(MuonBroadcastEvent event);
    public MuonResult get(String resourceQuery);
    public MuonResult post(String resource, MuonResourceEvent payload);
    public MuonResult put(String resource, MuonResourceEvent payload);

    public void shutdown();

    public static class MuonResult {
        private boolean success;

        private MuonResourceEvent event;

        public MuonResourceEvent getResponseEvent() {
            return event;
        }

        public void setEvent(MuonResourceEvent event) {
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
