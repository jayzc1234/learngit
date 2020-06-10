package com.zxs.rxjava;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * 测试
 * @author zc
 */
public class MapTest {

    public static void main(String[] args) {
        Observable.just(1,2,3).map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                return integer * 10;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
}
