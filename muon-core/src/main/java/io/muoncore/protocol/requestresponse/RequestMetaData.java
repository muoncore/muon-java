package io.muoncore.protocol.requestresponse;

public class RequestMetaData {

    private String url;
    private String sourceService;

    public RequestMetaData(String url, String sourceService) {
        this.url = url;
        this.sourceService = sourceService;
    }

    public String getUrl() {
        return url;
    }

    public String getSourceService() {
        return sourceService;
    }
}
