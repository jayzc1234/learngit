package zxs.rxjava.filter;

import rx.Observable;
import rx.functions.Action1;

/**
 * 过滤连续的重复数据，不连续的话不会过滤
 * @author zc
 */
public class DistinctUntilChangedTest {

    public static void main(String[] args) {
        test2();
    }

    /**
     * 重复连续，会过滤
     */
    private static void test2() {
        Observable.just(1,2,2,2,3,4,4,4,5,7).distinctUntilChanged().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }

    /**
     * 重复不连续不会过滤
     */
    private static void test1() {
        Observable.just(1,2,3,4,5,6,7,1,2,3).distinctUntilChanged().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
}
