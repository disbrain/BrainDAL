package com.disbrain.dbmslayer.actors.brokers;

import akka.actor.UntypedActor;
import com.disbrain.dbmslayer.DbmsLayerProvider;
import com.disbrain.dbmslayer.DbmsQuery;
import com.disbrain.dbmslayer.descriptors.QueryGenericArgument;
import com.disbrain.dbmslayer.messages.QueryRequest;


public class GenericQueryBroker extends UntypedActor {

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
        if(message instanceof QueryGenericArgument)
        {
            QueryGenericArgument request =(QueryGenericArgument) message;
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
