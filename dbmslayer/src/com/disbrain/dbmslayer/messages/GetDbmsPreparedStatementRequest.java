package com.disbrain.dbmslayer.messages;

import com.disbrain.dbmslayer.descriptors.RequestModeDescription;
import com.disbrain.dbmslayer.descriptors.RequestModes;

public class GetDbmsPreparedStatementRequest {
    public final String query;
    public final RequestModeDescription properties;

    public GetDbmsPreparedStatementRequest(String query, RequestModes props) {
        this.query = query;
        properties = props.getProperty();
    }
}
