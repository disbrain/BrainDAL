package akkatemplate.messages;

import akkatemplate.descriptors.TagDescriptor;

import java.util.ArrayList;
import java.util.LinkedList;

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

    public GetTagsReply(LinkedList<Object> values) {
        if (values.size() > 0) {
            output = new ArrayList<TagDescriptor>(values.size() / out_columns_num);

            for (int idx = 0; idx < values.size(); idx += out_columns_num)
                output.add(new TagDescriptor(values.get(idx), values.get(idx + 1)));
        } else
            output = null;
    }
}
