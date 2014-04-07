package akkatemplate.descriptors;

import java.math.BigInteger;

public class TagDescriptor {
    public final long tag_id;
    public final String tag_name;

    public TagDescriptor(Object id, Object name) {
        this.tag_id = ((BigInteger) id).longValue();
        this.tag_name = (String) name;
    }
}
