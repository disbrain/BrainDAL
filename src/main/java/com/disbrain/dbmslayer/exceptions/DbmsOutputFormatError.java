package com.disbrain.dbmslayer.exceptions;


@SuppressWarnings("serial")
public class DbmsOutputFormatError extends DbmsException {

    private final static int code = 7;
    private final static String description = "Error while parsing DB output";

    public DbmsOutputFormatError() {
        super();
    }

    public DbmsOutputFormatError(Exception ex) {
        super(ex);
    }

    public DbmsOutputFormatError(Exception ex, String extra_msg) {
        super(ex,extra_msg);
    }

    public DbmsOutputFormatError(String extra_info) {
        super(extra_info);
    }
    public String getMessage() {
        return description + super.getVerboseMessage();
    }


    public int getErrorCode() {
        return code;
    }


}
