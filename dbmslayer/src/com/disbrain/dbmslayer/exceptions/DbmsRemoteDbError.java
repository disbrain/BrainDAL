package com.disbrain.dbmslayer.exceptions;


@SuppressWarnings("serial")
public class DbmsRemoteDbError extends DbmsException {
    StackTraceElement trace[] = Thread.currentThread().getStackTrace();
    private Exception error;
    private final static int code = 1;
    private final static String description = "Error communicating with remote DB Host ";
    private String extraInfo = "";
    private String real_message = null;

    public DbmsRemoteDbError() {
        super();
    }

    public DbmsRemoteDbError(Exception ex) {
        super(ex);
    }

    public DbmsRemoteDbError(Exception ex, String extra_msg) {
        super(ex,extra_msg);
    }

    public DbmsRemoteDbError(String extra_info) {
        super(extra_info);
    }

    public String getMessage() {
        return description + super.getVerboseMessage();
    }

    public int getErrorCode() {
        return code;
    }

}
