package com.disbrain.dbmslayer.actors;


import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.LoggingAdapter;
import com.disbrain.dbmslayer.DbmsLayer;
import com.disbrain.dbmslayer.descriptors.ConnectionTweaksDescriptor;
import com.disbrain.dbmslayer.descriptors.EnqueuedRequestDescriptor;
import com.disbrain.dbmslayer.exceptions.DbmsException;
import com.disbrain.dbmslayer.exceptions.DbmsLayerError;
import com.disbrain.dbmslayer.exceptions.DbmsRemoteDbError;
import com.disbrain.dbmslayer.exceptions.DbmsRemoteQueryError;
import com.disbrain.dbmslayer.messages.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class DBMSWorker extends UntypedActor {

    private Statement running_stmt = null;
    private PreparedStatement prepared_running_stmt = null;

    private final static DbmsException UNKNOWN_REQUEST_RESOURCE = new DbmsLayerError("UNKNOWN REQUEST RESOURCE"),
            UNKNOWN_REQUEST_TYPOLOGY = new DbmsLayerError("UNKNOWN REQUEST TYPOLOGY");

    private Connection dbms_connection = null;
    private final ConnectionTweaksDescriptor connection_params;
    private LinkedList<EnqueuedRequestDescriptor> backlog_messages = new LinkedList<>();
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
        Object output_data;

        do {

            if (message instanceof GetDbmsConnectionReply) {
                dbms_connection = ((GetDbmsConnectionReply) message).connection;
                for (EnqueuedRequestDescriptor previous_message : backlog_messages)
                    getSelf().tell(previous_message.payload, previous_message.sender);
                backlog_messages.clear();
                return;
            }

            if (message instanceof DbmsException) {
                getContext().parent().tell(message, getSelf());
                return;
            }

            if (dbms_connection == null) {
                /* re enqueue to myself is a connection is not yet available */
                backlog_messages.add(new EnqueuedRequestDescriptor(message, getSender()));
                return;
            }

            if (message instanceof AsyncForwardRequest) {
                switch (((AsyncForwardRequest) message).routing_data.behaviour) {
                    case RESOURCE_GETTER:
                        /* SOMEONE ELSE WILL SEND THE REPLY */
                        DbmsLayer.DbmsLayerProvider.get(getContext().system()).getAsyncGetTasksBroker().forward(((AsyncForwardRequest) message).request, getContext());
                        return;
                    case RESOURCE_RELEASER:
                        DbmsLayer.DbmsLayerProvider.get(getContext().system()).getAsyncReleaseTasksBroker().forward(((AsyncForwardRequest) message).request, getContext());
                        return;
                    case RESOURCE_WHATEVER:
                        DbmsLayer.DbmsLayerProvider.get(getContext().system()).getAsyncWhateverTasksBroker().forward(((AsyncForwardRequest) message).request, getContext());
                        return;
                    default:
                        output_data = UNKNOWN_REQUEST_RESOURCE;
                }
                break;
            }

            if (message instanceof DbmsExecuteStmtRequest) {

                running_stmt = ((DbmsExecuteStmtRequest) message).stmt;

                try {

                    running_stmt.getConnection().setAutoCommit(((DbmsExecuteStmtRequest) message).autocommit);

                    switch (((DbmsExecuteStmtRequest) message).request_opts.typology) {
                        case WRITE:
                            output_data = new DBMSReply(
                                    running_stmt.executeUpdate(((DbmsExecuteStmtRequest) message).query),
                                    ((DbmsExecuteStmtRequest) message).request_opts.typology);
                            break;
                        case READ_ONLY:
                        case READ_WRITE:
                            output_data = new DBMSReply(
                                    running_stmt.executeQuery(((DbmsExecuteStmtRequest) message).query),
                                    ((DbmsExecuteStmtRequest) message).request_opts.typology);
                            break;
                        case ASYNC_READ_ONLY:
                        case ASYNC_WRITE:
                        case ASYNC_READ_WRITE:
                            getSelf().tell(new AsyncForwardRequest(message, ((DbmsExecuteStmtRequest) message).request_opts), getSender());
                            return;
                        default:
                            output_data = UNKNOWN_REQUEST_TYPOLOGY;
                            break;
                    }
                } catch (SQLException sql_exc) {
                    output_data = new DbmsRemoteQueryError(sql_exc);
                }
                break;
            }

            if (message instanceof DbmsExecutePrepStmtRequest) {
                DbmsExecutePrepStmtRequest exec_msg = (DbmsExecutePrepStmtRequest) message;

                prepared_running_stmt = exec_msg.stmt;

                try {
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
                            getSelf().tell(new AsyncForwardRequest(message, ((DbmsExecutePrepStmtRequest) message).request_opts), getSender());
                            return;
                        default:
                            output_data = UNKNOWN_REQUEST_TYPOLOGY;
                            break;
                    }
                } catch (SQLException sql_exc) {
                    output_data = new DbmsRemoteQueryError(sql_exc);
                }
                break;
            }

            if (message instanceof GetDbmsStatementRequest) {
                GetDbmsStatementRequest request = (GetDbmsStatementRequest) message;
                try {
                    output_data = new GetDbmsStatementReply(dbms_connection, request);
                } catch (SQLException sql_exc) {
                    output_data = new DbmsLayerError(sql_exc);
                    break;
                }
                //running_stmt = ((GetDbmsStatementReply) output_data).stmt;
                break;
            }

            if (message instanceof GetDbmsPreparedStatementRequest) {
                GetDbmsPreparedStatementRequest request = (GetDbmsPreparedStatementRequest) message;
                try {
                    output_data = new GetDbmsPreparedStatementReply(dbms_connection, request);
                } catch (SQLException sql_exc) {
                    output_data = new DbmsLayerError(sql_exc);
                    break;
                }
                //prepared_running_stmt = ((GetDbmsPreparedStatementReply) output_data).p_stmt;
                break;
            }

            if (message instanceof DbmsWorkerCmdRequest) {
                DbmsWorkerCmdRequest cmd_request = (DbmsWorkerCmdRequest) message;
                output_data = new DbmsWorkerCmdReply(((DbmsWorkerCmdRequest) message).request);
                try {
                    switch (cmd_request.request) {
                    /*
                    http://dev.mysql.com/doc/refman/5.5/en/metadata-locking.html
                    http://stackoverflow.com/questions/19203455/jdbc-mysql-lock-tables-freeze
                     */
                        case ROLLBACK:
                            dbms_connection.rollback();
                            break;
                        case COMMIT:
                            dbms_connection.commit();
                            break;
                        case CLOSE_STMT:
                            closeStatement();
                            break;
                        default:
                            output_data = new DbmsLayerError(String.format("Unhandled Command DBMSWorker: %s\n", message.getClass()));

                    }
                } catch (SQLException sql_exc) {
                    output_data = new DbmsRemoteDbError(sql_exc);
                }
                break;
            }

            output_data = new DbmsLayerError(String.format("Unhandled in DBMSWorker: %s\n", message.getClass().getName()));

        } while (false);

        getSender().tell(output_data, getSelf());

    }

    @Override
    public void postStop() {
        closeStatement();
        if (dbms_connection != null)
            DbmsLayer.DbmsLayerProvider.get(getContext().system()).getCloseConnectionsBroker().tell(new CloseDbmsConnectionRequest(dbms_connection), ActorRef.noSender());

    }

}
