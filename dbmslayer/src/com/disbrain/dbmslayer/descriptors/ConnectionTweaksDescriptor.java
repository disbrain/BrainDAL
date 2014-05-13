package com.disbrain.dbmslayer.descriptors;

/**
 * Created by angel on 13/05/14.
 */
public class ConnectionTweaksDescriptor {

    public final int isolation_level;

    public ConnectionTweaksDescriptor(int isolation_level) {
        this.isolation_level = isolation_level;
    }
}
