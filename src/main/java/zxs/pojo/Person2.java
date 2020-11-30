package zxs.pojo;

import org.springframework.core.Ordered;

public class Person2 implements Ordered {
    @Override
    public int getOrder() {
        return 1111;
    }
}
