package io.muoncore.crud.codec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Map;

public class KryoBinaryCodec implements BinaryCodec {

    @Override
    public <T> T decode(byte[] encodedData, Class<T> type) {
        Kryo kryo = new Kryo();

        Input input = new Input(encodedData);
        T decodedObject = kryo.readObject(input, type);
        input.close();

        return decodedObject;
    }

    @Override
    public Map decode(byte[] encodedData) {
        throw new IllegalArgumentException("KryoCodec cannot decode to generic Map");
    }

    @Override
    public byte[] encode(Object data) {
        Kryo kryo = new Kryo();
        Output output = new Output();
        output.setBuffer(new byte[5000]);
        kryo.writeObject(output, data);
        output.close();
        return output.getBuffer();
    }

    @Override
    public String getContentType() {
        return "x-muon/kryo";
    }
}
