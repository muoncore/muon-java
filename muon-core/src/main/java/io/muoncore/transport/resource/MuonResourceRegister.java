package io.muoncore.transport.resource;

public class MuonResourceRegister {

    private String resource;
    private String verb;

    public MuonResourceRegister(String resource, String verb) {
        this.resource = resource;
        this.verb = verb;
    }

    public String getResource() {
        return resource;
    }

    public String getVerb() {
        return verb;
    }
}
