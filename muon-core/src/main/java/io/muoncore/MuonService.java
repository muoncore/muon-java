package io.muoncore;

import io.muoncore.codec.Codecs;
import io.muoncore.future.MuonFuture;
import io.muoncore.transport.MuonEventTransport;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.resource.MuonResourceEvent;
import io.muoncore.transport.stream.MuonStreamRegister;

import java.net.URISyntaxException;
import java.util.List;

public interface MuonService extends MuonClient {

    public <T> void receive(String event, final Class<T> type, MuonListener<T> listener);
    public <T> void onQuery(String resource, final Class<T> type, MuonQuery<T> listener);
    public <T> void onCommand(String resource, final Class<T> type, MuonCommand<T> listener);

    public void start() throws URISyntaxException;

    public String getServiceIdentifer();

    public void setServiceIdentifer(String serviceIdentifer);

    public List<String> getTags();
    public Codecs getCodecs();
    void registerTransport(MuonEventTransport transport);
    public List<MuonStreamRegister> getStreams();

    public static interface MuonListener<T> {
        public void onEvent(MuonMessageEvent<T> event);
    }
    public static interface MuonQuery<T> {
        public MuonFuture<?> onQuery(MuonResourceEvent<T> queryEvent);
    }
    public static interface MuonCommand<T> {
        public MuonFuture<?> onCommand(MuonResourceEvent<T> queryEvent);
    }
}
