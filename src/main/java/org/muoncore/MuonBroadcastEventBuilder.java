package org.muoncore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MuonBroadcastEventBuilder {

    String eventName;
    Object content;
    String mimeType;
    List<String[]> headers = new ArrayList<String[]>();

    public static MuonBroadcastEventBuilder broadcast(String name) {
        MuonBroadcastEventBuilder builder = new MuonBroadcastEventBuilder();
        builder.eventName = name;
        return builder;
    }

    public MuonBroadcastEventBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public MuonBroadcastEventBuilder withMimeType(String type) {
        mimeType = type;
        return this;
    }

    public MuonBroadcastEventBuilder withHeader(String key, String value) {

        headers.add(new String[] { key, value });

        return this;
    }

    public MuonBroadcastEvent build() {
        MuonBroadcastEvent ev = new MuonBroadcastEvent(eventName, mimeType, content);
        for(String[] header: headers) {
            ev.addHeader(header[0], header[1]);
        }
        return ev;
    }
}
