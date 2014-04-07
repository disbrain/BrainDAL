package com.disbrain.dbmslayer.messages;

import com.disbrain.dbmslayer.descriptors.RequestModes;

public class QueryRequest {
    public final String query;
    public final RequestModes modes;
    public final Class<?> reply_type;
    public final boolean autocommit;
    public final Object[] args;

    public QueryRequest(String query, RequestModes modes, Class<?> reply_type, boolean autocommit, Object[] args) {
        this.query = query;
        this.modes = modes;
        this.reply_type = reply_type;
        this.autocommit = autocommit;
        this.args = args;
    }

    public QueryRequest(String query, RequestModes modes, Class<?> reply_type, boolean autocommit) {
        this.query = query;
        this.modes = modes;
        this.reply_type = reply_type;
        this.autocommit = autocommit;
        this.args = null;
    }

}
