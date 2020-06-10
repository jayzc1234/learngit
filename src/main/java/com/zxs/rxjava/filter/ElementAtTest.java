package com.zxs.rxjava.filter;

import rx.Observable;
import rx.functions.Action1;

/**
 * 取某一个元素
 * @author zc
 */
public class ElementAtTest {
    public static void main(String[] args) {
        Observable.just(1,2,3,4).elementAt(1).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
}
