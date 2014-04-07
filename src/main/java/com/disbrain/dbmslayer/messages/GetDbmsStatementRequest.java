package com.disbrain.dbmslayer.messages;

import com.disbrain.dbmslayer.descriptors.RequestModeDescription;
import com.disbrain.dbmslayer.descriptors.RequestModes;

public class GetDbmsStatementRequest {

    public final RequestModeDescription properties;

    public GetDbmsStatementRequest(RequestModes props) {
        properties = props.getProperty();

    }
}
