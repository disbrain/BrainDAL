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

    public QueryGenericArgument(ActorRef father, DbmsLayerProvider.DeathPolicy policy, String query, RequestModes request_properties, boolean autocommit, Class<?> reply_type, Object[] arg_array) {
        this.query = query;
        this.deathPolicy = policy;
        this.request_properties = request_properties;
        this.reply_type = reply_type;
        this.arg_array = arg_array;
        this.autocommit = autocommit;
        this.real_requester = father;
    }


    public QueryGenericArgument(ActorRef father, String query, RequestModes request_properties, boolean autocommit, Class<?> reply_type, Object[] arg_array) {
        this.query = query;
        this.request_properties = request_properties;
        this.reply_type = reply_type;
        this.arg_array = arg_array;
        this.autocommit = autocommit;
        this.real_requester = father;
        this.deathPolicy = DbmsLayerProvider.DeathPolicy.SURVIVE;
    }

    public QueryGenericArgument(ActorRef father, String query, RequestModes request_properties, boolean autocommit, Class<?> reply_type) {
        this.query = query;
        this.request_properties = request_properties;
        this.reply_type = reply_type;
        this.arg_array = null;
        this.autocommit = autocommit;
        this.real_requester = father;
        this.deathPolicy  = DbmsLayerProvider.DeathPolicy.SURVIVE;
    }

    public QueryGenericArgument(ActorRef father, String query, RequestModes request_properties, Class<?> reply_type, Object[] arg_array) {
        this.query = query;
        this.request_properties = request_properties;
        this.reply_type = reply_type;
        this.arg_array = arg_array;
        this.real_requester = father;
        this.autocommit = false;
        this.deathPolicy = DbmsLayerProvider.DeathPolicy.SURVIVE;
    }

    public QueryGenericArgument(ActorRef father, String query, RequestModes request_properties, Class<?> reply_type) {
        this.query = query;
        this.request_properties = request_properties;
        this.reply_type = reply_type;
        this.autocommit = false;
        this.real_requester = father;
        this.arg_array = null;
        this.deathPolicy  = DbmsLayerProvider.DeathPolicy.SURVIVE;
    }

}
