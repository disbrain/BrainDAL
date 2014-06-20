package akkatemplate.messages;

import akkatemplate.descriptors.TagDescriptor;
import com.disbrain.dbmslayer.descriptors.QueryGenericArgument;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: angel
 * Date: 02/01/14
 * Time: 13.21
 * To change this template use File | Settings | File Templates.
 */
public class GetTagsReply {
    public final static int out_columns_num = 2;
    public final static int out_lines_num = Integer.MAX_VALUE;
    public final ArrayList<TagDescriptor> output;

    public GetTagsReply(QueryGenericArgument request, Object[] values) {
        if (values.length > 0) {
            output = new ArrayList<TagDescriptor>(values.length / out_columns_num);
            for (int idx = 0; idx < values.length; idx += out_columns_num)
                output.add(new TagDescriptor(values[idx], values[idx + 1]));
        } else
            output = null;
    }
}
