package com.disbrain.dbmslayer.messages;

import java.sql.Connection;

public class CloseDbmsConnectionRequest {
    public final Connection connection;
    public final boolean shall_evict;

    public CloseDbmsConnectionRequest(boolean shall_evict, Connection conn) {
        this.shall_evict = shall_evict;
        connection = conn;
    }
}
