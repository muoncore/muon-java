package org.muoncore;

public interface MuonService extends MuonClient {

    public void receive(String event, MuonListener listener);
    public void resource(String resource, String descriptor, MuonGet listener);
    public void resource(String resource, String descriptor, MuonPost listener);
    public void resource(String resource, String descriptor, MuonPut listener);
    public void resource(String resource, String descriptor, MuonDelete listener);

    public void registerExtension(MuonExtension extension);

    public String getServiceIdentifer();

    //todo, should this be in the interface?
    public void setServiceIdentifer(String serviceIdentifer);

    public static interface MuonListener {
        public void onEvent(MuonBroadcastEvent event);
    }
    public static interface MuonGet {
        public Object onQuery(MuonResourceEvent queryEvent);
    }
    public static interface MuonPost {
        public Object onCommand(MuonResourceEvent queryEvent);
    }
    public static interface MuonPut {
        public Object onCommand(MuonResourceEvent queryEvent);
    }
    public static interface MuonDelete {
        public Object onCommand(MuonResourceEvent queryEvent);
    }
}
