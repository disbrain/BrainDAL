package akkatemplate.messages;

import com.disbrain.dbmslayer.descriptors.QueryGenericArgument;

/**
 * Created with IntelliJ IDEA.
 * User: angel
 * Date: 30/12/13
 * Time: 11.24
 * To change this template use File | Settings | File Templates.
 */
public class DummyOutputReply {
    public static final int out_columns_num = 1;
    public static final int out_lines_num = Integer.MAX_VALUE;
    public final Object[] output;

    public DummyOutputReply(QueryGenericArgument request, Object[] values) {
        output = values;
    }
}
