package com.zxs.rxjava;


import rx.Observable;
import rx.Subscriber;

import java.util.Random;

public class RxJavaTest1 {
    public static void main(String[] args) {
        createObserver().subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println("onError："+e);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("onNext接收到数据："+integer);
            }
        });
    }
    private static Observable<Integer> createObserver(){
        return Observable.create(new Observable.OnSubscribe<Integer>(){
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                if (!subscriber.isUnsubscribed()){
                    for(int i=0;i<5;i++){
                        int temp = new Random().nextInt(10);
                        if (temp>8){
                            subscriber.onError(new Throwable("value>8"));
                            break;
                        }else {
                            subscriber.onNext(temp);
                        }
                        if (i==4){
                            subscriber.onCompleted();
                        }
                    }
                }
            }
        });
    }


}
