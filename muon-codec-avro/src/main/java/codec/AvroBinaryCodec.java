package codec;

import io.muoncore.codec.BinaryCodec;

import java.util.Map;

public class AvroBinaryCodec implements BinaryCodec {

    @Override
    public <T> T decode(byte[] encodedData, Class<T> type) {
        throw new IllegalArgumentException("Not Implemented");
    }

    @Override
    public Map decode(byte[] encodedData) {
        throw new IllegalArgumentException("Not Implemented");
    }

    @Override
    public byte[] encode(Object data) {
        throw new IllegalArgumentException("Not Implemented");
    }

    @Override
    public String getContentType() {
        return "x-muon/avro";
    }
}
