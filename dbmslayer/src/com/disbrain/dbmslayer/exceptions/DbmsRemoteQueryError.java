package com.disbrain.dbmslayer.exceptions;

@SuppressWarnings("serial")
public class DbmsRemoteQueryError extends DbmsException {

    private final static int code = 2;
    private final static String description = "Error performing a query on remote db ";

    public DbmsRemoteQueryError() {
        super();
    }

    public DbmsRemoteQueryError(Exception ex) {
        super(ex);
    }

    public DbmsRemoteQueryError(Exception ex, String extra_msg) {
        super(ex,extra_msg);
    }

    public DbmsRemoteQueryError(String extra_info) {
        super(extra_info);
    }

    public String getMessage() {

        return description + super.getVerboseMessage();
    }

    public int getErrorCode() {
        return code;
    }
}
