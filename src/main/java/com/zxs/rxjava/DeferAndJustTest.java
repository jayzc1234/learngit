package com.zxs.rxjava;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;

/**
 * 测试
 * @author zc
 */
public class DeferAndJustTest {

    public static void main(String[] args) {
        Observable<Long> just = getJust();
        Observable<Long> defer = getDefer();

        for (int i=0;i<5;i++){
            just.subscribe(new Action1<Long>() {
                @Override
                public void call(Long aLong) {
                    System.out.println("just:"+aLong);
                }
            });

            defer.subscribe(new Action1<Long>() {
                @Override
                public void call(Long aLong) {
                    System.out.println("defer:"+aLong);
                }
            });
        }
    }

    private static Observable<Long> getJust(){
        return Observable.just(System.currentTimeMillis());
    }

    private static Observable<Long> getDefer(){
        return Observable.defer(new Func0<Observable<Long>>() {
            @Override
            public Observable<Long> call() {
                return getJust();
            }
        });
    }
}
