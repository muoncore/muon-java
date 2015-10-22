package io.muoncore;

import io.muoncore.codec.Codecs;
import io.muoncore.future.MuonFuture;
import io.muoncore.transport.MuonEventTransport;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.resource.MuonResourceEvent;
import io.muoncore.transport.stream.MuonStreamRegister;
import org.reactivestreams.Publisher;

import java.net.URISyntaxException;
import java.util.List;

public interface MuonService extends MuonClient {

    <T> void onQuery(String resource, final Class<T> type, MuonQuery<T> listener);
    <T> void onCommand(String resource, final Class<T> type, MuonCommand<T> listener);
    <T> void streamSource(String streamName, Class<T> type, MuonStreamGenerator<T> generator);
    <T> void streamSource(String streamName, Class<T> type, Publisher<T> pub);

    void start() throws URISyntaxException;

    String getServiceIdentifer();

    void setServiceIdentifer(String serviceIdentifer);

    List<String> getTags();
    Codecs getCodecs();
    void registerTransport(MuonEventTransport transport);
    List<MuonStreamRegister> getStreams();

    interface MuonListener<T> {
        void onEvent(MuonMessageEvent<T> event);
    }
    interface MuonQuery<T> {
        MuonFuture<?> onQuery(MuonResourceEvent<T> queryEvent);
    }
    interface MuonCommand<T> {
        MuonFuture<?> onCommand(MuonResourceEvent<T> queryEvent);
    }
}
