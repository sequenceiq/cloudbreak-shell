package com.sequenceiq.cloudbreak.shell.util;

import java.util.Map;

import groovyx.net.http.HttpResponseException;

public final class MessageUtil {
    private MessageUtil() {
    }

    public static String getMessage(Exception exception) {
        if (exception instanceof HttpResponseException) {
            return String.valueOf(((Map) ((HttpResponseException) exception).getResponse().getData()).get("message"));
        }
        return exception.getMessage();
    }
}
