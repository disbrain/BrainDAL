package com.disbrain.dbmslayer.descriptors;

import com.disbrain.dbmslayer.hotcache.BrainDbmsHotCache;

import java.sql.ResultSet;

public class RequestModes {
    public enum RequestTypology {READ_ONLY, READ_WRITE, WRITE, ASYNC_READ_ONLY, ASYNC_READ_WRITE, ASYNC_WRITE}

    ;

    public enum RequestBehaviour {RESOURCE_GETTER, RESOURCE_RELEASER, RESOURCE_WHATEVER}

    ;

    public final RequestTypology typology;
    public final RequestBehaviour behaviour;

    public RequestModes(RequestTypology typo) {
        typology = typo;
        this.behaviour = RequestBehaviour.RESOURCE_WHATEVER;
    }

    public RequestModes(RequestTypology typo, RequestBehaviour behaviour) {
        typology = typo;
        this.behaviour = behaviour;
    }

    public RequestModeDescription getProperty() {
        RequestModeDescription properties;

        switch (typology) {
            default:
            case ASYNC_READ_ONLY:
            case ASYNC_READ_WRITE:
            case READ_ONLY:
            case READ_WRITE:
            case ASYNC_WRITE:
            case WRITE:
                properties = BrainDbmsHotCache.polyvalent_request_mode_description;
                break;

        }
        return (properties);

    }
}
