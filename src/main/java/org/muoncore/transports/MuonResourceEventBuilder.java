package org.muoncore.transports;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MuonResourceEventBuilder {

    String url;
    Object content;
    String mimeType;
    List<String[]> headers = new ArrayList<String[]>();

    public static MuonResourceEventBuilder textMessage(String text) {
        MuonResourceEventBuilder builder = new MuonResourceEventBuilder();
        builder.content = text;
        return builder;
    }

    public MuonResourceEventBuilder withMimeType(String type) {
        mimeType = type;
        return this;
    }

    public MuonResourceEventBuilder withHeader(String key, String value) {

        headers.add(new String[] { key, value });

        return this;
    }

    public MuonResourceEventBuilder withUri(String uri) {
        this.url = uri;
        return this;
    }

    public MuonResourceEvent build() {
        try {
            URI uri = null;
            if (url != null) uri = new URI(url);
            MuonResourceEvent ev = new MuonResourceEvent(uri, mimeType, content);
            for(String[] header: headers) {
                ev.addHeader(header[0], header[1]);
            }
            return ev;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URI Is not valid", e);
        }
    }
}
