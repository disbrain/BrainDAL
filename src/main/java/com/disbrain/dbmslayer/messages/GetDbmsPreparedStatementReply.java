package com.disbrain.dbmslayer.messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GetDbmsPreparedStatementReply {
    public final PreparedStatement p_stmt;

    public GetDbmsPreparedStatementReply(Connection conn, GetDbmsPreparedStatementRequest request) throws SQLException {
        if (request.properties == null)
            p_stmt = conn.prepareStatement(request.query);
        else
            p_stmt = conn.prepareStatement(request.query, request.properties.resultSetType, request.properties.resultSetConcurrency, request.properties.resultSetHoldability);
    }
}