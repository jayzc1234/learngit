package com.zxs.rxjava;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * 测试
 * @author zc
 */
public class RangeObservableTest {
    public static void main(String[] args) {
        test2();
    }

    private static void test1() {
        Observable.range(1,10).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError");
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("onNext:"+integer);
            }
        });
    }

    private static void test2() {
        Observable.range(1,10).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
}
