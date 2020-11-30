package zxs.rxjava.filter;

import rx.Observable;
import rx.functions.Func1;

/**
 * 阻塞observable测试
 * @author zc
 */
public class BlockingObservableTest {

    public static void main(String[] args) {
        Integer first = Observable.just(1, 2, 3, 4, 5).toBlocking().first(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer > 4;
            }
        });
        System.out.println(first);
    }
}
