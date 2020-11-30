package zxs.rxjava.filter;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * 把源数据发到filter函数中 ，返回true则放行
 * @author zc
 */
public class FilterTest {
    public static void main(String[] args) {
        Observable.from(new Integer[]{1,2,3,4,5}).filter(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer % 2 == 0;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
}
