package org.muoncore.transports;


import java.util.HashMap;
import java.util.Map;

public class MuonMessageEvent {
    private String eventName;
    private Map<String, String> headers = new HashMap<String, String>();
    private Object decodedContent;
    private byte[] encodedBinaryContent;
    private String encodedStringContent;

    public MuonMessageEvent(String eventName, Object decodedContent) {
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

    public MuonMessageEvent(Object decodedContent) {
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

    public Object getDecodedContent() {
        return decodedContent;
    }

    public byte[] getEncodedBinaryContent() {
        return encodedBinaryContent;
    }

    public void setEncodedBinaryContent(byte[] encodedBinaryContent) {
        this.encodedBinaryContent = encodedBinaryContent;
    }

    public String getEncodedStringContent() {
        return encodedStringContent;
    }

    public void setEncodedStringContent(String encodedStringContent) {
        this.encodedStringContent = encodedStringContent;
    }
}
