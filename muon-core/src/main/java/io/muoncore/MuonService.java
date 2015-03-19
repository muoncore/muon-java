package io.muoncore;

import io.muoncore.codec.Codecs;
import io.muoncore.transport.MuonEventTransport;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.resource.MuonResourceEvent;
import io.muoncore.transport.stream.MuonStreamRegister;

import java.net.URISyntaxException;
import java.util.List;

public interface MuonService extends MuonClient {

    public <T> void receive(String event, final Class<T> type, MuonListener<T> listener);
    public <T> void onQueue(String queue, final Class<T> type, MuonListener<T> listener);
    public <T> void onGet(String resource, final Class<T> type, MuonGet<T> listener);
    public <T> void onPost(String resource, final Class<T> type, MuonPost<T> listener);
    public <T> void onPut(String resource, final Class<T> type, MuonPut<T> listener);
    public <T> void onDelete(String resource, final Class<T> type, MuonDelete<T> listener);

    public void start() throws URISyntaxException;

    public String getServiceIdentifer();

    //todo, should this be in the interface?
    public void setServiceIdentifer(String serviceIdentifer);

    public List<String> getTags();
    public Codecs getCodecs();
    void registerTransport(MuonEventTransport transport);
    public List<MuonStreamRegister> getStreams();

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
