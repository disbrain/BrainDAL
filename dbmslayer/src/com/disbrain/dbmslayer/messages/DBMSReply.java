package com.disbrain.dbmslayer.messages;

import com.disbrain.dbmslayer.descriptors.RequestModes;

import java.sql.ResultSet;

public class DBMSReply {

    public final RequestModes.RequestTypology request_mode;

    public final ResultSet resultSet;
    public final int ddl_retval;

    public DBMSReply(ResultSet res, RequestModes.RequestTypology mode) {
        request_mode = mode;
        resultSet = res;
        ddl_retval = -1;
    }

    public DBMSReply(int ret, RequestModes.RequestTypology mode) {
        request_mode = mode;
        ddl_retval = ret;
        resultSet = null;
    }
}
