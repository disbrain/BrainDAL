package akkatemplate.messages;

/**
 * Created with IntelliJ IDEA.
 * User: angel
 * Date: 02/01/14
 * Time: 12.25
 * To change this template use File | Settings | File Templates.
 */
public class GetLockReply {
    public final static int out_columns_num = 1;
    public final boolean got_lock;

    public GetLockReply(Object[] values) {
        if (values.length > 0)
            got_lock = ((Long) values[0]) > 0;
        else
            got_lock = false;
    }
}
