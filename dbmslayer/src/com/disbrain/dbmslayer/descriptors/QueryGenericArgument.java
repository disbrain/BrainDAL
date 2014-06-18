package com.disbrain.dbmslayer.descriptors;

import akka.actor.ActorRef;
import com.disbrain.dbmslayer.DbmsLayerProvider;

public class QueryGenericArgument {


    public final String query;
    public final RequestModes request_properties;
    public final Class<?> reply_type;
    public final Object arg_array[];
    public final boolean autocommit;
    public final ActorRef real_requester;
    public final DbmsLayerProvider.DeathPolicy deathPolicy;
    public final ConnectionTweaksDescriptor connection_params;
    public Object memento_data = null;

    public QueryGenericArgument(ActorRef father, DbmsLayerProvider.DeathPolicy policy, String query, RequestModes request_properties, boolean autocommit, Class<?> reply_type, Object[] arg_array) {
        this.query = query;
        this.deathPolicy = policy;
        this.request_properties = request_properties;
        this.reply_type = reply_type;
        this.arg_array = arg_array;
        this.autocommit = autocommit;
        this.real_requester = father;
        this.connection_params = null;
    }


    public QueryGenericArgument(ActorRef father, String query, RequestModes request_properties, boolean autocommit, Class<?> reply_type, Object[] arg_array) {
        this.query = query;
        this.request_properties = request_properties;
        this.reply_type = reply_type;
        this.arg_array = arg_array;
        this.autocommit = autocommit;
        this.real_requester = father;
        this.deathPolicy = DbmsLayerProvider.DeathPolicy.SURVIVE;
        this.connection_params = null;

    }

    public QueryGenericArgument(ActorRef father, String query, RequestModes request_properties, boolean autocommit, Class<?> reply_type) {
        this.query = query;
        this.request_properties = request_properties;
        this.reply_type = reply_type;
        this.arg_array = null;
        this.autocommit = autocommit;
        this.real_requester = father;
        this.deathPolicy = DbmsLayerProvider.DeathPolicy.SURVIVE;
        this.connection_params = null;
    }

    public QueryGenericArgument(ActorRef father, String query, RequestModes request_properties, Class<?> reply_type, Object[] arg_array) {
        this.query = query;
        this.request_properties = request_properties;
        this.reply_type = reply_type;
        this.arg_array = arg_array;
        this.real_requester = father;
        this.autocommit = false;
        this.deathPolicy = DbmsLayerProvider.DeathPolicy.SURVIVE;
        this.connection_params = null;
    }

    public QueryGenericArgument(ActorRef father, String query, RequestModes request_properties, Class<?> reply_type) {
        this.query = query;
        this.request_properties = request_properties;
        this.reply_type = reply_type;
        this.autocommit = false;
        this.real_requester = father;
        this.arg_array = null;
        this.deathPolicy = DbmsLayerProvider.DeathPolicy.SURVIVE;
        this.connection_params = null;
    }

    public QueryGenericArgument(QueryGenericArgument old_argument, ConnectionTweaksDescriptor tweaker) {
        this.query = old_argument.query;
        this.request_properties = old_argument.request_properties;
        this.reply_type = old_argument.reply_type;
        this.autocommit = old_argument.autocommit;
        this.real_requester = old_argument.real_requester;
        this.arg_array = old_argument.arg_array;
        this.deathPolicy = old_argument.deathPolicy;
        this.connection_params = tweaker;
    }

    public QueryGenericArgument setConnectionTweaks(ConnectionTweaksDescriptor tweaks) {
        return new QueryGenericArgument(this, tweaks);
    }

    public QueryGenericArgument setMementoData(Object data) {
        this.memento_data = data;
        return this;
    }

}
