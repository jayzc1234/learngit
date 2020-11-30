package zxs.rxjava;

import rx.Observable;
import rx.functions.Action1;

/**
 * @author zc
 */
public class WindowObservableTest {

    public static void main(String[] args) {
        windowObservable().subscribe(new Action1<Observable<Integer>>() {
            @Override
            public void call(Observable<Integer> integerObservable) {
                integerObservable.subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        System.out.println(integer);
                    }
                });
            }
        });
    }
    private static Observable windowObservable(){
        return Observable.just(1,2,3,4,5,6,7,8,9).window(2,3);
    }
}
