package akkatemplate.messages;

import java.util.LinkedList;


public class GetLockReply {
    public final static int out_columns_num = 1;
    public final boolean got_lock;

    public GetLockReply(LinkedList<Object> values) {
        if (values.size() > 0)
            got_lock = ((Long) values.getFirst()) > 0;
        else
            got_lock = false;
    }
}
