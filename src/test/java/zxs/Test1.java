package zxs;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Test1 implements Delayed {
    private volatile int i;

    public synchronized void setSync() {
         i++;
    }

    public  long getSync() {
        return i;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 1;
    }

    @Override
    public int compareTo(Delayed o) {
        return 0;
    }
}
