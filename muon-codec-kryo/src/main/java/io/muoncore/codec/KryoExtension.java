package io.muoncore.codec;

import io.muoncore.MuonExtension;
import io.muoncore.MuonExtensionApi;
import io.muoncore.MuonService;

public class KryoExtension implements MuonExtension {

    @Override
    public void extend(MuonService muonApi) {
        KryoBinaryCodec codec = new KryoBinaryCodec();


    }
}
