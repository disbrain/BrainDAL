package com.disbrain.dbmslayer.hotcache;


import com.disbrain.dbmslayer.descriptors.RequestModeDescription;
import com.disbrain.dbmslayer.descriptors.RequestModes;
import com.disbrain.dbmslayer.exceptions.DbmsMalformedReplyType;
import com.disbrain.dbmslayer.messages.DbmsWorkerCmdRequest;
import com.disbrain.dbmslayer.messages.GetDbmsStatementRequest;

import java.sql.ResultSet;

public class BrainDbmsHotCache {

    //Cached Commands
    public final static DbmsWorkerCmdRequest    close_statement_command =
            new DbmsWorkerCmdRequest(DbmsWorkerCmdRequest.Command.CLOSE_STMT);

    //Cached Request Properties
    public final static RequestModeDescription polyvalent_request_mode_description =
            new RequestModeDescription(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);

    //Cached Statement Fetching
    public final static GetDbmsStatementRequest common_get_statement_requester =
            new GetDbmsStatementRequest(new RequestModes(RequestModes.RequestTypology.READ_WRITE));

    //Cached Exceptions
    public final static DbmsMalformedReplyType  malformed_reply_type_found = new DbmsMalformedReplyType();

}
