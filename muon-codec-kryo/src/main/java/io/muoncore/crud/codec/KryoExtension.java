package io.muoncore.crud.codec;

import io.muoncore.MuonExtension;
import io.muoncore.crud.MuonService;

public class KryoExtension implements MuonExtension {

    @Override
    public void extend(MuonService muonApi) {
        KryoBinaryCodec codec = new KryoBinaryCodec();


    }
}
