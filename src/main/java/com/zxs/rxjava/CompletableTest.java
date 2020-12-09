package com.zxs.rxjava;

import rx.Completable;
import rx.CompletableSubscriber;
import rx.Subscription;

/**
 * 测试
 * @author zc
 */
public class CompletableTest {
    public static void main(String[] args) {
        Completable.error(new Throwable("completable error")).subscribe(new CompletableSubscriber() {
            @Override
            public void onCompleted() {
                System.out.println("error-onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("error-onError");
            }

            @Override
            public void onSubscribe(Subscription d) {

            }
        });

        Completable.complete().subscribe(new CompletableSubscriber() {
            @Override
            public void onCompleted() {
                System.out.println("complete-onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("complete-onError");
            }


            @Override
            public void onSubscribe(Subscription d) {

            }
        });
    }
}
