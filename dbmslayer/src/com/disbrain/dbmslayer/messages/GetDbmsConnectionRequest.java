package com.disbrain.dbmslayer.messages;

import com.disbrain.dbmslayer.descriptors.ConnectionTweaksDescriptor;

public class GetDbmsConnectionRequest {
    public final ConnectionTweaksDescriptor connection_params;

    public GetDbmsConnectionRequest(ConnectionTweaksDescriptor connection_params) {
        this.connection_params = connection_params;
    }
}
