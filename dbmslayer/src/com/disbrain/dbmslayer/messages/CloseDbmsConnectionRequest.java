package com.disbrain.dbmslayer.messages;

import java.sql.Connection;

public class CloseDbmsConnectionRequest {
    public final Connection connection;

    public CloseDbmsConnectionRequest(Connection conn) {
        connection = conn;
    }
}
