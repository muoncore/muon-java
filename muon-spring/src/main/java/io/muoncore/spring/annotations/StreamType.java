package io.muoncore.spring.annotations;

public enum StreamType {
    HOT("hot"), COLD("cold");
    private String name;

    StreamType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
