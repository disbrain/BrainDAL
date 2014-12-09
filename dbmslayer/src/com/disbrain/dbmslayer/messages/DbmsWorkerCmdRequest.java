package com.disbrain.dbmslayer.messages;

public class DbmsWorkerCmdRequest {
    public static enum Command {COMMIT, ROLLBACK, CLOSE_STMT, DESTROY_CONNECTION}

    ;
    public final Command request;

    public DbmsWorkerCmdRequest(Command request) {
        this.request = request;
    }
}
