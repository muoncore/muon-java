package io.muoncore.protocol.requestresponse;

public class Headers {

    private String url;
    private String sourceService;
    private String targetService;

    public Headers(String url,
                   String sourceService,
                   String targetService) {
        this.url = url;
        this.sourceService = sourceService;
        this.targetService = targetService;
    }

    public String getTargetService() {
        return targetService;
    }

    public String getUrl() {
        return url;
    }

    public String getSourceService() {
        return sourceService;
    }
}
