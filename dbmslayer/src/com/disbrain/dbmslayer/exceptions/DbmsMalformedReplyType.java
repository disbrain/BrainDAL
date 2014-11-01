package com.disbrain.dbmslayer.exceptions;


public class DbmsMalformedReplyType extends DbmsException {

    private final static int code = 8;
    private final static String description = "Malformed Reply Type has been supplied, unable to find a valid internal structure";

    public DbmsMalformedReplyType() {
        super();
    }

    public DbmsMalformedReplyType(Exception ex) {
        super(ex);
    }

    public DbmsMalformedReplyType(Exception ex, String extra_msg) {
        super(ex,extra_msg);
    }

    public DbmsMalformedReplyType(String extra_info) {
        super(extra_info);
    }

    public String getMessage() {
        return description + super.getVerboseMessage();
    }

    public int getErrorCode() {
        return code;
    }
}
