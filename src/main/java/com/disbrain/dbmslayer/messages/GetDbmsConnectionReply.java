package com.disbrain.dbmslayer.messages;

import java.sql.Connection;


public class GetDbmsConnectionReply {
    public final Connection connection;

    public GetDbmsConnectionReply(Connection conn) {
        this.connection = conn;
    }
}
