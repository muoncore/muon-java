package org.muoncore.transports;

import java.util.ArrayList;
import java.util.List;

public class MuonMessageEventBuilder {

    String eventName;
    Object content = "";
    String mimeType;
    List<String[]> headers = new ArrayList<String[]>();

    public static MuonMessageEventBuilder named(String name) {
        MuonMessageEventBuilder builder = new MuonMessageEventBuilder();
        builder.eventName = name;
        return builder;
    }

    public MuonMessageEventBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public MuonMessageEventBuilder withNoContent() {
        this.content = "";
        return this;
    }

    public MuonMessageEventBuilder withMimeType(String type) {
        mimeType = type;
        return this;
    }

    public MuonMessageEventBuilder withHeader(String key, String value) {

        headers.add(new String[] { key, value });

        return this;
    }

    public MuonMessageEvent build() {
        MuonMessageEvent ev = new MuonMessageEvent(eventName, mimeType, content);
        for(String[] header: headers) {
            ev.addHeader(header[0], header[1]);
        }
        return ev;
    }
}
