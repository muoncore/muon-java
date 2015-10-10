package io.muoncore.crud;

import io.muoncore.MuonStreamGenerator;
import io.muoncore.crud.codec.Codecs;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.crud.stream.MuonStreamRegister;
import org.reactivestreams.Publisher;

import java.net.URISyntaxException;
import java.util.List;

public class MuonServer implements MuonService {

    @Override
    public <T> void onQuery(String resource, Class<T> type, MuonQueryListener<T> listener) {

    }

    @Override
    public <T> void onCommand(String resource, Class<T> type, MuonCommandListener<T> listener) {

    }

    @Override
    public <T> void streamSource(String streamName, MuonStreamGenerator<MuonClient.MuonEvent<T>> generator) {

    }

    @Override
    public <T> void streamSource(String streamName, Publisher<MuonClient.MuonEvent<T>> pub) {

    }

    @Override
    public void start() throws URISyntaxException {

    }

    @Override
    public String getServiceIdentifer() {
        return null;
    }

    @Override
    public void setServiceIdentifer(String serviceIdentifer) {

    }

    @Override
    public List<String> getTags() {
        return null;
    }

    @Override
    public Codecs getCodecs() {
        return null;
    }

    @Override
    public void registerTransport(MuonTransport transport) {

    }

    @Override
    public List<MuonStreamRegister> getStreams() {
        return null;
    }
}
