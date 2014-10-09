package org.muoncore;

public interface MuonClient {

    public void emit(String eventName, MuonEvent event);
    public MuonResult get(String resourceQuery);
    public MuonResult post(String resource, MuonEvent payload);
    public MuonResult put(String resource, MuonEvent payload);

    public static class MuonResult {
        private boolean success;

        private MuonEvent event;

        public MuonEvent getEvent() {
            return event;
        }

        public void setEvent(MuonEvent event) {
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
