package com.disbrain.dbmslayer.exceptions;


@SuppressWarnings("serial")
public class DbmsLayerError extends DbmsException {


    private final static int code = 4;
    private final static String description = "The following error has occurred executing an api call: ";

    public DbmsLayerError() {
        super();
    }

    public DbmsLayerError(Exception ex) {
        super(ex);
    }

    public DbmsLayerError(Exception ex, String extra_msg) {
        super(ex,extra_msg);
    }

    public DbmsLayerError(String extra_info) {
        super(extra_info);
    }

    public String getMessage() {
        return description + super.getVerboseMessage();
    }

    public int getErrorCode() {
        return code;
    }

}
