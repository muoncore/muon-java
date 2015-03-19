package codec;

import io.muoncore.MuonExtension;
import io.muoncore.MuonService;

public class AvroExtension implements MuonExtension {

    @Override
    public void extend(MuonService muonApi) {
        AvroBinaryCodec codec = new AvroBinaryCodec();


    }
}
