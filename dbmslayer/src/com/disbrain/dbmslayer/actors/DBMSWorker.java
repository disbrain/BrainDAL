package com.disbrain.dbmslayer.actors;


import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.DbmsLayer;
import com.disbrain.dbmslayer.descriptors.ConnectionTweaksDescriptor;
import com.disbrain.dbmslayer.exceptions.DbmsException;
import com.disbrain.dbmslayer.exceptions.DbmsLayerError;
import com.disbrain.dbmslayer.exceptions.DbmsRemoteDbError;
import com.disbrain.dbmslayer.exceptions.DbmsRemoteQueryError;
import com.disbrain.dbmslayer.messages.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBMSWorker extends UntypedActor {

    private Statement running_stmt = null;
    private PreparedStatement prepared_running_stmt = null;

    private Connection dbms_connection = null;
    private final ConnectionTweaksDescriptor connection_params;
    private ArrayList<Object> messages = new ArrayList<Object>();
    private ArrayList<ActorRef> messages_senders = new ArrayList<ActorRef>();
    private final LoggingAdapter log;

    public DBMSWorker(ConnectionTweaksDescriptor connection_params) {
        this.connection_params = connection_params;
        this.log = DbmsLayer.DbmsLayerProvider.get(getContext().system()).getLoggingAdapter();
    }

    public void closeStatement() {
        try {
            if (running_stmt != null) {
                running_stmt.close();
                running_stmt = null;
            }
            if (prepared_running_stmt != null) {
                prepared_running_stmt.close();
                prepared_running_stmt = null;
            }
        } catch (SQLException ex) {
            log.error(ex, "CAUGHT EXCEPTION WHILE CLOSING STATMENT");
        }
    }

    @Override
    public void preStart() {
        DbmsLayer.DbmsLayerProvider.get(getContext().system()).getConnectionsBroker().tell(new GetDbmsConnectionRequest(connection_params), getSelf());
    }

    @Override
    public void onReceive(Object message) {
        Object output_data = null;
        int fsm_stage = 0;


        try {

            do {

                if (message instanceof GetDbmsConnectionReply) {
                    int prev_requests = messages.size();
                    dbms_connection = ((GetDbmsConnectionReply) message).connection;
                    for (int cur_req = 0; cur_req < prev_requests; cur_req++) {
                        getSelf().tell(messages.remove(0), messages_senders.remove(0));


                    }
                    return;
                }

                if (message instanceof DbmsException) {
                    getContext().parent().tell(message, getSelf());
                    return;
                }

                if (dbms_connection == null) {
                    /* re enqueue to myself */
                    messages.add(message);
                    messages_senders.add(getSender());

                    return;
                }

                if (message instanceof DbmsExecuteStmtRequest) {

                    DbmsExecuteStmtRequest exec_msg = (DbmsExecuteStmtRequest) message;
                    fsm_stage = 1;
                    running_stmt = exec_msg.stmt;

                    running_stmt.getConnection().setAutoCommit(exec_msg.autocommit);
                    switch (exec_msg.request_opts.typology) {
                        case WRITE:
                            output_data = new DBMSReply(running_stmt.executeUpdate(exec_msg.query), exec_msg.request_opts.typology);
                            break;
                        case READ_ONLY:
                        case READ_WRITE:
                            output_data = new DBMSReply(running_stmt.executeQuery(exec_msg.query), exec_msg.request_opts.typology);
                            break;
                        case ASYNC_READ_ONLY:
                        case ASYNC_WRITE:
                        case ASYNC_READ_WRITE:
                            switch (exec_msg.request_opts.behaviour) {
                                case RESOURCE_GETTER:
                                    /* SOMEONE ELSE WILL SEND THE REPLY */
                                    DbmsLayer.DbmsLayerProvider.get(getContext().system()).getAsyncGetTasksBroker().forward(exec_msg, getContext());
                                    return;
                                case RESOURCE_RELEASER:
                                    /* SOMEONE ELSE WILL SEND THE REPLY */
                                    DbmsLayer.DbmsLayerProvider.get(getContext().system()).getAsyncReleaseTasksBroker().forward(exec_msg, getContext());
                                    return;
                                case RESOURCE_WHATEVER:
                                    /* SOMEONE ELSE WILL SEND THE REPLY */
                                    DbmsLayer.DbmsLayerProvider.get(getContext().system()).getAsyncWhateverTasksBroker().forward(exec_msg, getContext());
                                    return;
                                default:
                                    output_data = new DbmsLayerError("UNKNOWN REQUEST RESOURCE");
                            }

                            break;
                        default:
                            output_data = new DbmsLayerError("UNKNOWN REQUEST TYPOLOGY");
                            break;

                    }

                    break;
                }

                if (message instanceof DbmsExecutePrepStmtRequest) {
                    DbmsExecutePrepStmtRequest exec_msg = (DbmsExecutePrepStmtRequest) message;
                    fsm_stage = 4;
                    prepared_running_stmt = exec_msg.stmt;
                    prepared_running_stmt.getConnection().setAutoCommit(exec_msg.autocommit);
                    switch (exec_msg.request_opts.typology) {
                        case WRITE:
                            output_data = new DBMSReply(prepared_running_stmt.executeUpdate(), exec_msg.request_opts.typology);
                            break;
                        case READ_ONLY:
                        case READ_WRITE:
                            output_data = new DBMSReply(prepared_running_stmt.executeQuery(), exec_msg.request_opts.typology);
                            break;
                        case ASYNC_READ_ONLY:
                        case ASYNC_WRITE:
                        case ASYNC_READ_WRITE:
                            switch (exec_msg.request_opts.behaviour) {
                                case RESOURCE_GETTER:
                                    /* SOMEONE ELSE WILL SEND THE REPLY */
                                    DbmsLayer.DbmsLayerProvider.get(getContext().system()).getAsyncGetTasksBroker().forward(exec_msg, getContext());
                                    return;
                                case RESOURCE_RELEASER:
                                    /* SOMEONE ELSE WILL SEND THE REPLY */
                                    DbmsLayer.DbmsLayerProvider.get(getContext().system()).getAsyncReleaseTasksBroker().forward(exec_msg, getContext());
                                    return;
                                case RESOURCE_WHATEVER:
                                    /* SOMEONE ELSE WILL SEND THE REPLY */
                                    DbmsLayer.DbmsLayerProvider.get(getContext().system()).getAsyncWhateverTasksBroker().forward(exec_msg, getContext());
                                    return;
                                default:
                                    output_data = new DbmsLayerError("UNKNOWN REQUEST RESOURCE");
                            }

                            break;
                        default:
                            output_data = new DbmsLayerError("UNKNOWN REQUEST TYPOLOGY");
                            break;
                    }

                    break;

                }

                if (message instanceof GetDbmsStatementRequest) {
                    GetDbmsStatementRequest request = (GetDbmsStatementRequest) message;
                    fsm_stage = 2;
                    output_data = new GetDbmsStatementReply(dbms_connection, request);
                    running_stmt = ((GetDbmsStatementReply) output_data).stmt;
                    break;
                }

                if (message instanceof GetDbmsPreparedStatementRequest) {
                    GetDbmsPreparedStatementRequest request = (GetDbmsPreparedStatementRequest) message;
                    output_data = new GetDbmsPreparedStatementReply(dbms_connection, request);
                    prepared_running_stmt = ((GetDbmsPreparedStatementReply) output_data).p_stmt;
                    break;
                }

                if (message instanceof DbmsWorkerCmdRequest) {
                    DbmsWorkerCmdRequest cmd_request = (DbmsWorkerCmdRequest) message;
                    output_data = new DbmsWorkerCmdReply(((DbmsWorkerCmdRequest) message).request);
                    fsm_stage = 5;
                    switch (cmd_request.request) {
                        /*
                        http://dev.mysql.com/doc/refman/5.5/en/metadata-locking.html
                        http://stackoverflow.com/questions/19203455/jdbc-mysql-lock-tables-freeze
                         */
                        case ROLLBACK:
                            dbms_connection.rollback();
                            break;
                        case COMMIT:
                            if (dbms_connection.getAutoCommit() == false)
                                dbms_connection.commit();
                            break;
                        case CLOSE_STMT:
                            closeStatement();
                            break;
                        default:
                            DbmsException error = new DbmsLayerError(String.format("Unhandled Command DBMSWorker: %s\n", message.getClass()));
                            output_data = error;

                    }

                    break;
                }

                output_data = new DbmsLayerError(String.format("Unhandled in DBMSWorker: %s\n", message.getClass()));

            } while (false);


        } catch (SQLException ex) {
            DbmsException error = null;
            switch (fsm_stage) {
                case 1:
                case 4:
                    error = new DbmsRemoteQueryError(ex);
                    break;
                case 2:
                case 3:
                    error = new DbmsRemoteDbError(ex);
                    break;
                case 5:
                    error = new DbmsLayerError(ex);
            }
            output_data = error;

        }

        getSender().tell(output_data, getSelf());

    }

    @Override
    public void postStop() {
        closeStatement();
        if (dbms_connection != null)
            DbmsLayer.DbmsLayerProvider.get(getContext().system()).getCloseConnectionsBroker().tell(new CloseDbmsConnectionRequest(dbms_connection), ActorRef.noSender());

    }

}
