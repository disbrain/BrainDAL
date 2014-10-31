package com.disbrain.dbmslayer.descriptors;

public class ConnectionTweaksDescriptor {

    public final int isolation_level;

    public ConnectionTweaksDescriptor(int isolation_level) {
        this.isolation_level = isolation_level;
    }
}
