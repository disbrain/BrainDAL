package com.disbrain.dbmslayer.exceptions;

@SuppressWarnings("serial")
public class DbmsConnectionPoolError extends DbmsException {

    private final static int code = 6;
    private final static String description = "Error while requesting a new connection from the pool";

    public DbmsConnectionPoolError()
    {
        super();
    }

    public DbmsConnectionPoolError(Exception ex) {
        super(ex);
    }

    public DbmsConnectionPoolError(Exception ex, String extra_msg) {
        super(ex,extra_msg);
    }

    public DbmsConnectionPoolError(String extra_info) {
        super(extra_info);
    }

    public String getMessage() {
        return description + super.getVerboseMessage();
    }
    public int getErrorCode() {
        return code;
    }

}
