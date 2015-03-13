package org.muoncore;

import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.transports.MuonResourceEvent;

import java.net.URISyntaxException;

public interface MuonService extends MuonClient {

    public void receive(String event, MuonListener listener);
    public <T> void onQueue(String queue, final Class<T> type, MuonListener<T> listener);
    public <T> void onGet(String resource, final Class<T> type, MuonGet<T> listener);
    public <T> void onPost(String resource, final Class<T> type, MuonPost<T> listener);
    public <T> void onPut(String resource, final Class<T> type, MuonPut<T> listener);
    public <T> void onDelete(String resource, final Class<T> type, MuonDelete<T> listener);

    public void registerExtension(MuonExtension extension);
    public void start() throws URISyntaxException;

    public String getServiceIdentifer();

    //todo, should this be in the interface?
    public void setServiceIdentifer(String serviceIdentifer);

    public static interface MuonListener<T> {
        public void onEvent(MuonMessageEvent<T> event);
    }
    public static interface MuonGet<T> {
        public Object onQuery(MuonResourceEvent<T> queryEvent);
    }
    public static interface MuonPost<T> {
        public Object onCommand(MuonResourceEvent<T> queryEvent);
    }
    public static interface MuonPut<T> {
        public Object onCommand(MuonResourceEvent<T> queryEvent);
    }
    public static interface MuonDelete<T> {
        public Object onCommand(MuonResourceEvent<T> queryEvent);
    }
}
