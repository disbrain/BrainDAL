package com.disbrain.dbmslayer.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.DbmsLayer;
import com.disbrain.dbmslayer.DbmsLayerProvider;
import com.disbrain.dbmslayer.descriptors.ConnectionTweaksDescriptor;
import com.disbrain.dbmslayer.descriptors.QueryGenericArgument;
import com.disbrain.dbmslayer.descriptors.RequestModes;
import com.disbrain.dbmslayer.exceptions.DbmsException;
import com.disbrain.dbmslayer.exceptions.DbmsLayerError;
import com.disbrain.dbmslayer.exceptions.DbmsOutputFormatError;
import com.disbrain.dbmslayer.exceptions.DbmsRemoteDbError;
import com.disbrain.dbmslayer.messages.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;


public class GenericDBMSQueryingActor extends UntypedActor {

    private String query;

    private boolean prepared = false,
            autocommit = false;

    private ActorRef dbms_actor,
            real_requester;

    private Object[] values;

    private Class<?> rep_type;

    private RequestModes request_properties = null;

    private DbmsLayerProvider.DeathPolicy policy;

    private final LoggingAdapter log;

    private ConnectionTweaksDescriptor connection_params;

    private QueryGenericArgument gen_arg_request = null;

    private int rows_num,
            lines_num = 1;

    private void closeResult(ResultSet result) {
        try {
            result.close();
        } catch (SQLException ex) {
            log.error(ex, "UNEXPECTED ERROR CLOSING RESULTSET");
        }
    }

    public GenericDBMSQueryingActor(QueryGenericArgument gen_arg) {
        this.gen_arg_request = gen_arg;
        this.query = gen_arg.query;
        this.rep_type = gen_arg.reply_type;
        this.request_properties = gen_arg.request_properties;
        this.autocommit = gen_arg.autocommit;
        this.policy = gen_arg.deathPolicy;
        this.real_requester = gen_arg.real_requester;
        this.connection_params = gen_arg.connection_params;
        this.log = DbmsLayer.DbmsLayerProvider.get(getContext().system()).getLoggingAdapter();

        if (gen_arg.arg_array != null) {
            prepared = true;
            values = gen_arg.arg_array;
        }

    }

    private void start_fsm() {
        Object request;
        if (prepared == true)
            request = new GetDbmsPreparedStatementRequest(query, request_properties);
        else
            request = new GetDbmsStatementRequest(request_properties);
        dbms_actor.tell(request, getSelf());
    }

    private int fetch_reply_struct() {
        try {

            rows_num = rep_type.getField("out_columns_num").getInt(null);

        } catch (NoSuchFieldException | IllegalAccessException ex) {

            return (-1);
        }

        try {
            lines_num = rep_type.getField("out_lines_num").getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException ex) {

        }
        return (0);
    }

    @Override
    public void preStart() {
        if (fetch_reply_struct() == 0) {
            dbms_actor = getContext().actorOf(Props.create(DBMSWorker.class, connection_params), "DBMSWORKER");
            start_fsm();
        } else {
            real_requester.tell(new DbmsLayerError("UNABLE TO FETCH INFO FOR REPLY CONSTRUCTION"), getSelf());
            getContext().stop(getSelf());
        }
    }

    public boolean reincarnate(QueryGenericArgument gen_arg) {
        dbms_actor.tell(new DbmsWorkerCmdRequest(DbmsWorkerCmdRequest.Command.CLOSE_STMT), getSelf());
        gen_arg_request = gen_arg;
        prepared = false;
        this.policy = gen_arg.deathPolicy;
        this.autocommit = gen_arg.autocommit;
        this.query = gen_arg.query;
        this.rep_type = gen_arg.reply_type;
        this.request_properties = gen_arg.request_properties;
        this.real_requester = gen_arg.real_requester;

        if (gen_arg.real_requester == null)
            this.real_requester = getContext().parent();


        if (values != null)
            values = null;

        if (gen_arg.arg_array != null) {
            prepared = true;
            values = gen_arg.arg_array;
        }

        if (fetch_reply_struct() != 0)
            return false;

        start_fsm();
        return true;
    }

    @Override
    public void onReceive(Object message) {

        do {
            if (message instanceof QueryGenericArgument) {
                if (reincarnate((QueryGenericArgument) message) == false)
                    getSelf().tell(new DbmsLayerError("UNABLE TO FETCH INFO FOR REPLY CONSTRUCTION"), getSelf());
                break;
            }

            if (message instanceof GetDbmsStatementReply) {
                Statement stmt = ((GetDbmsStatementReply) message).stmt;
                getSender().tell(new DbmsExecuteStmtRequest(stmt, query, request_properties, autocommit), getSelf());
                break;
            }

            if (message instanceof GetDbmsPreparedStatementReply) {
                PreparedStatement p_stmt = ((GetDbmsPreparedStatementReply) message).p_stmt;

                try {

                    for (int param_idx = 0; param_idx < values.length; param_idx++)
                        p_stmt.setObject(param_idx + 1, values[param_idx]);

                    getSender().tell(new DbmsExecutePrepStmtRequest(p_stmt, request_properties, autocommit), getSelf());

                } catch (SQLException ex) {
                    real_requester.tell(new DbmsLayerError(ex), getSelf());
                    if (policy == DbmsLayerProvider.DeathPolicy.SUICIDE)
                        getContext().stop(getSelf());
                }

                break;
            }

            if (message instanceof DBMSReply) {
                boolean has_result = false;
                DBMSReply dbms_result = (DBMSReply) message;
                ResultSet result = dbms_result.resultSet;
                Object output_obj = null;
                LinkedList<Object> result_list = new LinkedList<>();
                try {

                    if (result != null) {

                        for (int cur_line = 0; cur_line < lines_num; cur_line++) {
                            if (result.next()) {
                                for (int cur_row = 0; cur_row < rows_num; cur_row++)
                                    result_list.add(result.getObject(cur_row + 1));
                            } else
                                break;
                        }

                    } else
                        result_list.add(dbms_result.ddl_retval);

                    if (gen_arg_request.isIncludedInReply())
                        output_obj = rep_type.getConstructor(QueryGenericArgument.class, Object[].class).newInstance(gen_arg_request, result_list.toArray());
                    else
                        output_obj = rep_type.getConstructor(Object[].class).newInstance(new Object[]{result_list.toArray()});

                } catch (Exception ex)//(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ex)
                {
                    if (ex instanceof SQLException) {
                        if (has_result == false)
                            output_obj = new DbmsRemoteDbError(ex);
                        else
                            output_obj = new DbmsOutputFormatError(ex);
                    } else
                        output_obj = new DbmsLayerError(ex, " INPUT CLASS: " + rep_type.getName() + " OUT_DATA ARG LEN: " + result_list.size());

                    ((DbmsException) output_obj).setOriginalRequest(gen_arg_request);

                } finally {

                    if (result != null)
                        closeResult(result);

                    real_requester.tell(output_obj, getSelf());
                    if (policy == DbmsLayerProvider.DeathPolicy.SUICIDE)
                        getContext().stop(getSelf());
                }
                break;
            }

            if (message instanceof DbmsWorkerCmdRequest) {
                dbms_actor.forward(message, getContext());
                break;
            }

            if (message instanceof DbmsWorkerCmdReply) {
                DbmsWorkerCmdReply reply = (DbmsWorkerCmdReply) message;

                if (reply.originalRequest != DbmsWorkerCmdRequest.Command.CLOSE_STMT)
                    log.error("UNEXPECTED BEHAVIOUR");
                break;
            }

            if (message instanceof DbmsException) {
                ((DbmsException) message).setOriginalRequest(gen_arg_request);
                real_requester.tell(message, getSelf());
                if (policy == DbmsLayerProvider.DeathPolicy.SUICIDE)
                    getContext().stop(getSelf());
                break;
            }

            real_requester.tell(new DbmsLayerError(message.getClass().getName() + " UNHANDLED RECEIVED"), getSelf());
            if (policy == DbmsLayerProvider.DeathPolicy.SUICIDE)
                getContext().stop(getSelf());

        } while (false);

    }

}
