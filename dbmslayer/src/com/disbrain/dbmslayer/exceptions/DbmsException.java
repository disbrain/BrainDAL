package com.disbrain.dbmslayer.exceptions;


import com.disbrain.dbmslayer.descriptors.QueryGenericArgument;

public abstract class DbmsException extends RuntimeException {

    protected StackTraceElement trace[] = Thread.currentThread().getStackTrace();
    protected Exception error = null;
    protected String extraInfo = "";
    protected String real_message = null;
    private QueryGenericArgument original_request;
    private int code = -1;

    protected DbmsException() {

    }

    protected DbmsException(Exception ex) {
        trace = Thread.currentThread().getStackTrace();
        error = ex;
    }

    protected DbmsException(Exception ex, String extra_msg) {
        error = ex;
        extraInfo = extra_msg;
    }

    protected DbmsException(String extra_msg) {
        extraInfo = extra_msg;
    }

    protected String getVerboseMessage() {
        if (error != null)
            extraInfo += String.format("\nClass: %s\nCause: %s\nMessage: %s\nStack Trace:\n", error.getClass(), error.getCause(), error.getMessage());
        for (StackTraceElement element : trace)
            extraInfo += element.toString() + "\n";
        return extraInfo;
    }

    public void setOriginalRequest(QueryGenericArgument request) {
        original_request = request;
    }

    public QueryGenericArgument getOriginalRequest() {
        return original_request;
    }

    public String getRealMessage() {
        if (error != null)
            real_message = error.getMessage();
        return real_message;
    }

    public int getErrorCode() {
        return code;
    }
}
