package zxs.rxjava.filter;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * 去重
 * @author zc
 */
public class DistinctTest {

    public static void main(String[] args) {

        Observable.just(1,2,3,4,5,5,1).distinct(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                if (integer == 1 || integer == 2){
                    return integer;
                }
                return 1;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }

    private static void test1() {
        Observable.just(1,2,3,4,5,5,1).distinct().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
}
