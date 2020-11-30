package zxs.rxjava.combine;

import rx.Observable;
import rx.Subscriber;

/**
 * 合并,merge将多个observable合并 不保证有顺序，两个observable的数据可能交错合并
 * @author zc
 */
public class MergeAndMergeDelayErrorTest {

    public static void main(String[] args) {
        mergeDelayErrorObserver().subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println(integer);
            }
        });
    }
    private static Observable<Integer> mergeObserver(){
      return Observable.merge(Observable.just(1,2),Observable.just(3,4));
    }

    private static Observable<Integer> mergeDelayErrorObserver(){
        return Observable.mergeDelayError(Observable.create(new Observable.OnSubscribe<Integer>(){

            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                for(int i=0 ;i<5 ;i++){
                    if (i == 3){
                        subscriber.onError(new Throwable("测试异常"));
                    }
                    subscriber.onNext(i);
                }
            }
        }),Observable.create(new Observable.OnSubscribe<Integer>(){
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                for(int i=0 ;i<5 ;i++){
                    subscriber.onNext(i);
                }
                subscriber.onCompleted();
            }
        }));
    }
}
