package org.muoncore.extension.amqp;

import org.muoncore.MuonExtension;
import org.muoncore.MuonExtensionApi;
import org.muoncore.extension.http.HttpEventTransport;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class AmqpTransportExtension implements MuonExtension {

    @Override
    public void init(MuonExtensionApi muonApi) {
        try {
            muonApi.addTransport(new AMQPEventTransport(muonApi.getMuon().getServiceIdentifer()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to start AMQP Extension", e);
        } catch (KeyManagementException e) {
            throw new IllegalStateException("Unable to start AMQP Extension", e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to start AMQP Extension", e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start AMQP Extension", e);
        }
    }

    @Override
    public String getName() {
        return "amqp/1.0";
    }
}
