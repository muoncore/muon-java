package io.muoncore.spring.controllers;

import java.util.concurrent.TimeUnit;

/**
 * Created by volod on 10/27/2015.
 */
public class MuonControllersConfigurationHolder {
    private Integer streamKeepAliveTimeout;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public Integer getStreamKeepAliveTimeout() {
        return streamKeepAliveTimeout;
    }

    public void setStreamKeepAliveTimeout(Integer streamKeepAliveTimeout) {
        this.streamKeepAliveTimeout = streamKeepAliveTimeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
