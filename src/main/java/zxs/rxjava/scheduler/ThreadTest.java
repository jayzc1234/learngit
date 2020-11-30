package zxs.rxjava.scheduler;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.IOException;

/**
 * @author zc
 */
public class ThreadTest {

    public static void main(String[] args) throws IOException {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("start:"+Thread.currentThread().getName());
                subscriber.onNext(1);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread()).
                map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        System.out.println("map1:"+Thread.currentThread().getName());
                        return integer+1;
                    }
                }).subscribeOn(Schedulers.io()).map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                System.out.println("map2:"+Thread.currentThread().getName());
                return integer+1;
            }
        }).subscribeOn(Schedulers.computation()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println("subscribe:"+Thread.currentThread().getName());
            }
        });
        System.in.read();
    }
}
