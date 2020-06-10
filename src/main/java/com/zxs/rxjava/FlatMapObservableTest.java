package com.zxs.rxjava;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试
 * @author zc
 */
public class FlatMapObservableTest {

    public static void main(String[] args) {
        flatMapIterableObserver().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        });
    }

    private static Observable<String> flatMapObserver(){
        return Observable.just(1,2,3).flatMap(new Func1<Integer, Observable<String>>() {
            @Override
            public Observable<String> call(Integer integer) {
                return Observable.from(new String[]{"flat map:"+integer,"flat map-"+integer+":"+integer});
            }
        });
    }

    private static Observable<String> flatMapIterableObserver(){
        return Observable.just(1,2,3).flatMapIterable(new Func1<Integer, Iterable<String>>() {
            @Override
            public Iterable<String> call(Integer integer) {
                List<String> data=new ArrayList<>();
                for(int i=0;i<1;i++){
                    data.add("flatMapIterable:"+integer);
                }
                return data;
            }
        });
    }

}
