package com.disbrain.dbmslayer;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.ExtensionIdProvider;


public class DbmsLayer extends AbstractExtensionId<DbmsLayerProvider> implements ExtensionIdProvider {
    public final static DbmsLayer DbmsLayerProvider = new DbmsLayer();

    private DbmsLayer() {
    }


    public DbmsLayerProvider createExtension(ExtendedActorSystem extendedActorSystem) {
        DbmsLayerProvider provider = null;
        try {
            provider = new DbmsLayerProvider(extendedActorSystem);
        } catch (Exception exc) {
            exc.printStackTrace();
            System.err.println("Unable to init DbmsLayer, halting: " + exc.getMessage());
            System.exit(123);
        }
        return provider;
    }

    public DbmsLayer lookup() {
        return DbmsLayer.DbmsLayerProvider;
    }
}
