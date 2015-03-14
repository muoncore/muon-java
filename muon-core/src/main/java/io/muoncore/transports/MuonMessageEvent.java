package io.muoncore.transports;


import java.util.HashMap;
import java.util.Map;

public class MuonMessageEvent<T> {
    private String eventName;
    private Map<String, String> headers = new HashMap<String, String>();
    private String contentType;
    private T decodedContent;
    private byte[] encodedBinaryContent;
    private String encodedStringContent;

    public MuonMessageEvent(String eventName, T decodedContent) {
        this.eventName = eventName;
        this.decodedContent = decodedContent;
    }
    public MuonMessageEvent(String eventName, byte[] encodedContent) {
        this.eventName = eventName;
        this.encodedBinaryContent = encodedContent;
    }
    public MuonMessageEvent(String eventName, String encodedContent) {
        this.eventName = eventName;
        this.encodedStringContent = encodedContent;
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

    public MuonMessageEvent(T decodedContent) {
        this.decodedContent = decodedContent;
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public String getEventName() {
        return eventName;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public T getDecodedContent() {
        return decodedContent;
    }

    public byte[] getBinaryEncodedContent() {
        return encodedBinaryContent;
    }

    public void setEncodedBinaryContent(byte[] encodedBinaryContent) {
        this.encodedBinaryContent = encodedBinaryContent;
    }

    public String getTextEncodedContent() {
        return encodedStringContent;
    }

    public void setEncodedStringContent(String encodedStringContent) {
        this.encodedStringContent = encodedStringContent;
    }
}
