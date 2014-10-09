package org.muoncore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MuonEventBuilder {

    String url;
    Object content;
    String mimeType;
    List<String[]> headers = new ArrayList<String[]>();

    public static MuonEventBuilder textMessage(String text) {
        MuonEventBuilder builder = new MuonEventBuilder();
        builder.content = text;
        return builder;
    }

    public MuonEventBuilder withMimeType(String type) {
        mimeType = type;
        return this;
    }

    public MuonEventBuilder withHeader(String key, String value) {

        headers.add(new String[] { key, value });

        return this;
    }

    public MuonEvent build() {
        try {
            MuonEvent ev = new MuonEvent(new URI(url), mimeType, content);
            for(String[] header: headers) {
                ev.addHeader(header[0], header[1]);
            }
            return ev;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URI Is not valid", e);
        }
    }
}
