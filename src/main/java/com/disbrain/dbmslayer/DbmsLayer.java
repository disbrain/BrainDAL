package com.disbrain.dbmslayer;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.ExtensionIdProvider;
import akka.event.Logging;
import akka.event.LoggingAdapter;


public class DbmsLayer extends AbstractExtensionId<DbmsLayerProvider> implements ExtensionIdProvider {
    public final static DbmsLayer DbmsLayerProvider = new DbmsLayer();

    private DbmsLayer() {
    }


    public DbmsLayerProvider createExtension(ExtendedActorSystem extendedActorSystem) {
        DbmsLayerProvider provider = null;
        try {
            provider = new DbmsLayerProvider(extendedActorSystem);
        } catch (Exception exc) {
            LoggingAdapter log = Logging.getLogger(extendedActorSystem, this);
            log.error(exc, "Unable to init DbmsLayer, halting!");
            System.exit(1);
        }
        return provider;
    }

    public DbmsLayer lookup() {
        return DbmsLayer.DbmsLayerProvider;
    }
}
