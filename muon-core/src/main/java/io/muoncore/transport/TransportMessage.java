package io.muoncore.transport;

import java.util.List;
import java.util.Map;

public class TransportMessage {
    private String eventType;
    private String id;
    private String targetServiceName;
    private String sourceServiceName;
    private String protocol;
    private Map<String, String> metadata;
    private byte[] payload;
    private String contentType;
    private List<String> sourceAvailableContentTypes;
    private ChannelOperation channelOperation = ChannelOperation.NORMAL;

    public TransportMessage(String type,
                            String id,
                            String targetServiceName,
                            String sourceServiceName,
                            String protocol,
                            Map<String, String> metadata,
                            String contentType,
                            byte[] payload,
                            List<String> sourceAvailableContentTypes) {
        this.targetServiceName = targetServiceName;
        this.eventType = type;
        this.id = id;
        this.sourceServiceName = sourceServiceName;
        this.protocol = protocol;
        this.metadata = metadata;
        this.payload = payload;
        this.contentType = contentType;
        this.sourceAvailableContentTypes = sourceAvailableContentTypes;
    }

    public TransportMessage(String type,
                            String id,
                            String targetServiceName,
                            String sourceServiceName,
                            String protocol,
                            Map<String, String> metadata,
                            String contentType,
                            byte[] payload,
                            List<String> sourceAvailableContentTypes,
                            ChannelOperation channelOperation) {
        this.targetServiceName = targetServiceName;
        this.eventType = type;
        this.id = id;
        this.sourceServiceName = sourceServiceName;
        this.protocol = protocol;
        this.metadata = metadata;
        this.payload = payload;
        this.contentType = contentType;
        this.sourceAvailableContentTypes = sourceAvailableContentTypes;
        this.channelOperation = channelOperation;
    }

    public ChannelOperation getChannelOperation() {
        return channelOperation;
    }

    public List<String> getSourceAvailableContentTypes() {
        return sourceAvailableContentTypes;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public String getType() {
        return eventType;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getPayload() {
        return payload;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getId() {
        return id;
    }

    public String getSourceServiceName() {
        return sourceServiceName;
    }

    public String getProtocol() {
        return protocol;
    }

    public enum ChannelOperation {
        /** Cause the channel to be closed after propogating this message **/
        CLOSE_CHANNEL,
        NORMAL
    }

    @Override
    public String toString() {
        return "TransportMessage{" +
                "eventType='" + eventType + '\'' +
                ", targetServiceName='" + targetServiceName + '\'' +
                ", sourceServiceName='" + sourceServiceName + '\'' +
                ", protocol='" + protocol + '\'' +
                ", metadata=" + metadata +
                ", channelOperation=" + channelOperation +
                '}';
    }
}
