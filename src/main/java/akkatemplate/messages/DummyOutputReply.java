package akkatemplate.messages;


public class DummyOutputReply {
    public static final int out_columns_num = 1;
    public static final int out_lines_num = Integer.MAX_VALUE;
    public final Object[] output;

    public DummyOutputReply(Object[] values) {
        output = values;
    }
}
