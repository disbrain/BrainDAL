package com.disbrain.dbmslayer.actors.brokers;

import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.UntypedActor;
import akka.japi.Function;
import com.disbrain.dbmslayer.DbmsLayerProvider;
import com.disbrain.dbmslayer.DbmsQuery;
import com.disbrain.dbmslayer.descriptors.QueryGenericArgument;
import com.disbrain.dbmslayer.messages.QueryRequest;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.stop;


public class GenericQueryBroker extends UntypedActor {

    private final static SupervisorStrategy strategy =
            new OneForOneStrategy(0, Duration.create(0, TimeUnit.SECONDS),
                    new Function<Throwable, SupervisorStrategy.Directive>() {
                        @Override
                        public SupervisorStrategy.Directive apply(Throwable throwable) throws Exception {
                            return stop();
                        }
                    });

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof QueryRequest) {
            QueryRequest request = (QueryRequest) message;
            DbmsQuery.create_generic_fsm(getContext(),
                    new QueryGenericArgument(getSender(),
                            DbmsLayerProvider.DeathPolicy.SUICIDE,
                            request.query,
                            request.modes,
                            request.autocommit,
                            request.reply_type,
                            request.args)
            );
        }
        if (message instanceof QueryGenericArgument) {
            QueryGenericArgument request = (QueryGenericArgument) message;
            DbmsQuery.create_generic_fsm(getContext(),
                    new QueryGenericArgument(request.real_requester,
                            DbmsLayerProvider.DeathPolicy.SUICIDE,
                            request.query,
                            request.request_properties,
                            request.autocommit,
                            request.reply_type,
                            request.arg_array)
            );
        }
    }
}
