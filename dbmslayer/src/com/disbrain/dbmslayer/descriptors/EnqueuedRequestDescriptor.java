package com.disbrain.dbmslayer.descriptors;

import akka.actor.ActorRef;

public class EnqueuedRequestDescriptor {
    public final Object payload;
    public final ActorRef sender;

    public EnqueuedRequestDescriptor(Object payload, ActorRef sender) {
        this.payload = payload;
        this.sender = sender;
    }
}
