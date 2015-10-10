package io.muoncore.transport.crud.requestresponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MuonResourceEventBuilder {

    private String url;
    private Object content;
    private List<String[]> headers = new ArrayList<String[]>();

    public static MuonResourceEventBuilder event(Object content) {
        MuonResourceEventBuilder builder = new MuonResourceEventBuilder();
        builder.content = content;
        return builder;
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
            MuonResourceEvent ev = new MuonResourceEvent(uri);
            for(String[] header: headers) {
                ev.addHeader(header[0], header[1]);
            }
            ev.setDecodedContent(content);
            return ev;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URI Is not valid", e);
        }
    }
}
