package io.muoncore.extension.amqp;

import io.muoncore.MuonExtension;
import io.muoncore.crud.MuonService;
import io.muoncore.config.MuonBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class AmqpTransportExtension implements MuonExtension {

    static {
        MuonBuilder.registerExtension(config -> new AmqpTransportExtension(config.getDiscoveryUrl()));
    }

    private String url;

    public AmqpTransportExtension(String url) {
        this.url = url;
    }

    @Override
    public void extend(MuonService muonApi) {
        try {
            AMQPEventTransport trans = new AMQPEventTransport(
                    url,
                    muonApi.getServiceIdentifer(),
                    muonApi.getTags(),
                    muonApi.getCodecs());

            muonApi.registerTransport(trans);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to start AMQP Transport", e);
        } catch (KeyManagementException e) {
            throw new IllegalStateException("Unable to start AMQP Transport", e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to start AMQP Transport", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start AMQP Transport", e);
        }
    }
}
