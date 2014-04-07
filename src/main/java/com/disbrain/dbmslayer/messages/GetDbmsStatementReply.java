package com.disbrain.dbmslayer.messages;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class GetDbmsStatementReply {
    public final Statement stmt;

    public GetDbmsStatementReply(Connection conn, GetDbmsStatementRequest request) throws SQLException {
        if (request.properties == null) {
            this.stmt = conn.createStatement();
        } else {
            this.stmt = conn.createStatement(request.properties.resultSetType, request.properties.resultSetConcurrency, request.properties.resultSetHoldability);
        }
    }
}
