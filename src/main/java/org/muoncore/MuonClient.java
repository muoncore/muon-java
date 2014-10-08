package org.muoncore;

public interface MuonClient {

    public void emit(String eventName, Object event);
    public MuonResult get(String resourceQuery);
    public MuonResult post(String resource, Object payload);
    public MuonResult put(String resource, Object payload);

    public static class MuonResult {
        private boolean success;

        private Object event;

        public Object getEvent() {
            return event;
        }

        public void setEvent(Object event) {
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
