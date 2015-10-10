package io.muoncore.crud;

import io.muoncore.MuonStreamGenerator;
import io.muoncore.crud.codec.Codecs;
import io.muoncore.future.MuonFuture;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.crud.MuonMessageEvent;
import io.muoncore.transport.crud.requestresponse.MuonResourceEvent;
import io.muoncore.transport.crud.stream.MuonStreamRegister;
import org.reactivestreams.Publisher;

import java.net.URISyntaxException;
import java.util.List;

public interface MuonService {

    <T> void onQuery(String resource, final Class<T> type, MuonQueryListener<T> listener);
    <T> void onCommand(String resource, final Class<T> type, MuonCommandListener<T> listener);
    <T> void streamSource(String streamName, MuonStreamGenerator<MuonClient.MuonEvent<T>> generator);
    <T> void streamSource(String streamName, Publisher<MuonClient.MuonEvent<T>> pub);

    void start() throws URISyntaxException;

    String getServiceIdentifer();

    void setServiceIdentifer(String serviceIdentifer);

    List<String> getTags();
    Codecs getCodecs();
    void registerTransport(MuonTransport transport);
    List<MuonStreamRegister> getStreams();

    interface MuonListener<T> {
        void onEvent(MuonMessageEvent<T> event);
    }
    interface MuonQueryListener<T> {
        MuonFuture<?> onQuery(MuonResourceEvent<T> queryEvent);
    }
    interface MuonCommandListener<T> {
        MuonFuture<?> onCommand(MuonResourceEvent<T> queryEvent);
    }
}
