package io.muoncore.transport;

import java.util.ArrayList;
import java.util.List;

public class MuonMessageEventBuilder {

    private String eventName;
    private Object decodedContent = "";
    List<String[]> headers = new ArrayList<String[]>();

    public static MuonMessageEventBuilder named(String name) {
        MuonMessageEventBuilder builder = new MuonMessageEventBuilder();
        builder.eventName = name;
        return builder;
    }

    public MuonMessageEventBuilder withContent(Object content) {
        this.decodedContent = content;
        return this;
    }

    public MuonMessageEventBuilder withNoContent() {
        this.decodedContent = null;
        return this;
    }

    public MuonMessageEventBuilder withHeader(String key, String value) {

        headers.add(new String[] { key, value });

        return this;
    }

    public MuonMessageEvent build() {
        MuonMessageEvent ev = new MuonMessageEvent(eventName, decodedContent);
        for(String[] header: headers) {
            ev.addHeader(header[0], header[1]);
        }
        return ev;
    }
}
