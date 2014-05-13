package com.disbrain.dbmslayer.messages;

import com.disbrain.dbmslayer.descriptors.ConnectionTweaksDescriptor;

import java.sql.Connection;
import java.sql.SQLException;


public class GetDbmsConnectionReply {
    public final Connection connection;

    public GetDbmsConnectionReply(Connection conn, ConnectionTweaksDescriptor connection_params) throws SQLException {
        this.connection = conn;
        if (connection_params != null) {
            conn.setTransactionIsolation(connection_params.isolation_level);
        }
    }
}
