package com.disbrain.dbmslayer;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.disbrain.dbmslayer.actors.GenericDBMSQueryingActor;
import com.disbrain.dbmslayer.descriptors.QueryGenericArgument;
import com.disbrain.dbmslayer.descriptors.RequestModes;


public class DbmsQuery {

    private DbmsQuery() {
    }

    public static ActorRef create_generic_fsm(ActorContext ctx, QueryGenericArgument request, String description) {
        return ctx.actorOf(Props.create(GenericDBMSQueryingActor.class, request), description);

    }

    public static ActorRef create_generic_fsm(ActorSystem system, QueryGenericArgument request) {
        return system.actorOf(Props.create(GenericDBMSQueryingActor.class, request));
    }

    public static ActorRef create_generic_fsm(ActorContext ctx, QueryGenericArgument request) {
        return ctx.actorOf(Props.create(GenericDBMSQueryingActor.class, request));

    }

    public static ActorRef reuse_fsm(ActorRef target, QueryGenericArgument new_command) {
        target.tell(new_command, ActorRef.noSender());
        return (target);
    }

    public static ActorRef async_reuse_fsm(ActorRef target, QueryGenericArgument old_command) {
        QueryGenericArgument new_command;
        RequestModes.RequestTypology new_typology = old_command.request_properties.typology;

        switch (old_command.request_properties.typology) {
            case READ_ONLY:
                new_typology = RequestModes.RequestTypology.ASYNC_READ_ONLY;
                break;
            case READ_WRITE:
                new_typology = RequestModes.RequestTypology.ASYNC_READ_WRITE;
                break;
            case WRITE:
                new_typology = RequestModes.RequestTypology.ASYNC_WRITE;
                break;
            default:
                System.err.println("ASYNCING AN ALREADY ASYNC REQUEST: " + old_command.query);
        }

        new_command = new QueryGenericArgument(old_command.real_requester,
                old_command.deathPolicy,
                old_command.query,
                new RequestModes(new_typology, old_command.request_properties.behaviour),
                old_command.autocommit,
                old_command.reply_type,
                old_command.arg_array);

        target.tell(new_command, ActorRef.noSender());
        return (target);
    }


}
