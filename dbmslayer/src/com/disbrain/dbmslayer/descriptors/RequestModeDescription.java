package com.disbrain.dbmslayer.descriptors;

public class RequestModeDescription {

    public final int resultSetType,
            resultSetConcurrency,
            resultSetHoldability;

    public RequestModeDescription(int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
    }

}
