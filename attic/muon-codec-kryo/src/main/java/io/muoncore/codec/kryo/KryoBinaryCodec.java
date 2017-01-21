package io.muoncore.codec.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.muoncore.codec.MuonCodec;
import io.muoncore.exception.MuonException;

import java.lang.reflect.Type;

public class KryoBinaryCodec implements MuonCodec {

    @Override
    public <T> T decode(byte[] encodedData, Type type) {
        if (!(type instanceof Class)) {
            throw new MuonException("Kryo codec does not support compound or parameterized types. Use plain classes instead");
        }
        Kryo kryo = new Kryo();

        Input input = new Input(encodedData);
        T decodedObject = kryo.readObject(input, (Class<T>) type);
        input.close();

        return decodedObject;
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
