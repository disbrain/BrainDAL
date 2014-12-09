package akkatemplate.messages;


import java.util.LinkedList;

public class DummyOutputReply {
    public static final int out_columns_num = 1;
    public static final int out_lines_num = Integer.MAX_VALUE;
    public final LinkedList<Object> output;

    public DummyOutputReply(LinkedList<Object> values) {
        output = values;
    }
}
