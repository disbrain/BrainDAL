package com.disbrain.dbmslayer.messages;


public class DbmsWorkerCmdReply {
    public final DbmsWorkerCmdRequest.Command originalRequest;

    public DbmsWorkerCmdReply(DbmsWorkerCmdRequest.Command request) {
        originalRequest = request;
    }
}
