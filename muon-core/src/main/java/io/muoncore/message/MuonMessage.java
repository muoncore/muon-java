package io.muoncore.message;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MuonMessage {

    private String id;
    private long created;
    @SerializedName("target_service")
    private String targetServiceName;
    @SerializedName("target_service_instance")
    private String targetInstance;
    @SerializedName("origin_service")
    private String sourceServiceName;
    private String protocol;
    private String step;
    private Status status;
    private byte[] payload;
    @SerializedName("content_type")
    private String contentType;
    @SerializedName("channel_op")
    private ChannelOperation channelOperation = ChannelOperation.normal;

    public MuonMessage(String id, long created, String targetServiceName, String targetInstance, String sourceServiceName, String protocol, String step, Status status, byte[] payload, String contentType, ChannelOperation channelOperation) {
        this.id = id;
        this.created = created;
        this.targetInstance = targetInstance;
        this.targetServiceName = targetServiceName;
        this.sourceServiceName = sourceServiceName;
        this.protocol = protocol;
        this.step = step;
        this.status = status;
        this.payload = payload;
        this.contentType = contentType;
        this.channelOperation = channelOperation;
    }

    public String getId() {
        return id;
    }

    public long getCreated() {
        return created;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public String getSourceServiceName() {
        return sourceServiceName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getStep() {
        return step;
    }

    public Status getStatus() {
        return status;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getContentType() {
        return contentType;
    }

    public ChannelOperation getChannelOperation() {
        return channelOperation;
    }

    public enum ChannelOperation {
        /** Cause the channel to be closed after propogating this message **/
        closed,
        normal
    }

    @Override
    public String toString() {
        return "MuonMessage{" +
                "id='" + id + '\'' +
                ", created=" + created +
                ", targetServiceName='" + targetServiceName + '\'' +
                ", sourceServiceName='" + sourceServiceName + '\'' +
                ", protocol='" + protocol + '\'' +
                ", step='" + step + '\'' +
                ", status=" + status +
                ", contentType='" + contentType + '\'' +
                ", channelOperation=" + channelOperation +
                '}';
    }

    public enum Status {
        success, failure, error
    }
}
