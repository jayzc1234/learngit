package zxs.rxjava.filter;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

import java.util.concurrent.TimeUnit;

/**
 * ces
 * @author zc
 */
public class ThrottleWithTimeoutTest {

    public static void main(String[] args) {
        throttleWithTimeoutObserver().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
   private static Observable<Integer> createObservable(){
       return Observable.create(new Observable.OnSubscribe<Integer>() {
           @Override
           public void call(Subscriber<? super Integer> subscriber) {
             for(int i=0; i<10; i++){
               if (!subscriber.isUnsubscribed()){
                   subscriber.onNext(i);
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
       });
   }

   private static Observable<Integer> throttleWithTimeoutObserver(){
       return createObservable().throttleWithTimeout(200, TimeUnit.MILLISECONDS);
   }

}
