package zxs.rxjava.filter;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.concurrent.TimeUnit;

/**
 * 未完成
 * 根据时间过滤
 * @author
 */
public class DebounceTest {

    public static void main(String[] args) {
        createObservable().subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                System.out.println(aLong);
            }
        });
    }
    private static Observable<Long> createObservable(){
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                for(int i=0; i<10; i++){
                    if (!subscriber.isUnsubscribed()){
                        subscriber.onNext(Long.parseLong(i+""));
                        int sleep=100;
                        if (sleep % 3 == 0){
                        }
                        sleep=300;

                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                subscriber.onCompleted();
            }
        }).debounce(new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                return Observable.timer(aLong % 2 ==0? aLong * 1500:100,TimeUnit.MILLISECONDS);
            }
        });
    }

    private  static Observable<Long> debounceObserver(){
        return Observable.interval(1000, TimeUnit.MILLISECONDS).debounce(new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                return Observable.timer(aLong % 2 * 1500,TimeUnit.MILLISECONDS);
            }
        });
    }
}
