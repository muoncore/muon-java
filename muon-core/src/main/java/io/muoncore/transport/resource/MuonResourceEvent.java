package io.muoncore.transport.resource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MuonResourceEvent<T> {
    private URI uri;
    private Map<String, String> headers = new HashMap<String, String>();
    private byte[] binaryEncodedContent;
    private String textEncodedContent;
    private T decodedContent;
    private String contentType;

    public <T> MuonResourceEvent(URI uri) {
        this.uri = uri;
    }

    public byte[] getBinaryEncodedContent() {
        return binaryEncodedContent;
    }

    public void setBinaryEncodedContent(byte[] binaryEncodedContent) {
        this.binaryEncodedContent = binaryEncodedContent;
    }

    public String getTextEncodedContent() {
        return textEncodedContent;
    }

    public void setTextEncodedContent(String textEncodedContent) {
        this.textEncodedContent = textEncodedContent;
    }

    public T getDecodedContent() {
        return decodedContent;
    }

    public void setDecodedContent(T decodedContent) {
        this.decodedContent = decodedContent;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public URI getUri() { return this.uri; }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public String getResource() {
        return uri.getPath();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getServiceId() {
        return uri.getHost();
    }
}
