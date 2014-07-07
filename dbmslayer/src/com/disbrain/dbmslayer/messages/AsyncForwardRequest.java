package com.disbrain.dbmslayer.messages;

import com.disbrain.dbmslayer.descriptors.RequestModes;

public class AsyncForwardRequest {
    public final Object request;
    public final RequestModes routing_data;

    public AsyncForwardRequest(Object payload, RequestModes routing_data)

    {
        this.request = payload;
        this.routing_data = routing_data;
    }
}
