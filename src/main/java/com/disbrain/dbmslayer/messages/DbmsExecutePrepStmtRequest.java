package com.disbrain.dbmslayer.messages;

import com.disbrain.dbmslayer.descriptors.RequestModes;

import java.sql.PreparedStatement;

public class DbmsExecutePrepStmtRequest {
    public final PreparedStatement stmt;
    public final RequestModes request_opts;
    public final boolean autocommit;

    public DbmsExecutePrepStmtRequest(PreparedStatement stmt, RequestModes opts, boolean autocommit) {
        this.stmt = stmt;
        this.request_opts = opts;
        this.autocommit = autocommit;
    }
}
