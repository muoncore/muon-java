package org.muoncore;

public interface Muon {

    public void emit(String eventName, Object event);
    public MuonResult get(String resourceQuery);
    public MuonResult post(String resource, Object payload);
    public MuonResult put(String resource, Object payload);

    public void receive(String event, MuonListener listener);
    public void resource(String resource, String descriptor, MuonGet listener);
    public void resource(String resource, String descriptor, MuonPost listener);
    public void resource(String resource, String descriptor, MuonPut listener);
    public void resource(String resource, String descriptor, MuonDelete listener);

    public void registerExtension(MuonExtension extension);

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

    public static interface MuonListener {
        public void onEvent(Object event);
    }
    public static interface MuonGet {
        public Object onQuery(Object queryEvent);
    }
    public static interface MuonPost {
        public Object onCommand(Object queryEvent);
    }
    public static interface MuonPut {
        public Object onCommand(Object queryEvent);
    }
    public static interface MuonDelete {
        public Object onCommand(Object queryEvent);
    }
}
