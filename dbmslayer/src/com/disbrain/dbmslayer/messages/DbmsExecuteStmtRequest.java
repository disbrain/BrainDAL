package com.disbrain.dbmslayer.messages;

import com.disbrain.dbmslayer.descriptors.RequestModes;

import java.sql.Statement;

public class DbmsExecuteStmtRequest {
    public final Statement stmt;
    public final String query;
    public final RequestModes request_opts;
    public final boolean autocommit;

    public DbmsExecuteStmtRequest(Statement stmt, String query, RequestModes opts) {
        this.stmt = stmt;
        this.query = query;
        this.request_opts = opts;
        this.autocommit = false;

    }

    public DbmsExecuteStmtRequest(Statement stmt, String query, RequestModes opts, boolean autocommit) {
        this.stmt = stmt;
        this.query = query;
        this.request_opts = opts;
        this.autocommit = autocommit;

    }
}
