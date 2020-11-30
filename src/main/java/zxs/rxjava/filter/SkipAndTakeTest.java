package zxs.rxjava.filter;

import rx.Observable;
import rx.functions.Action1;

/**
 * 跳过和。。
 * @author zc
 */
public class SkipAndTakeTest {

    public static void main(String[] args) {
        skipObserver().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
    public static Observable<Integer> skipObserver(){
        return Observable.just(1,2,3,4).skip(2);
    }

    public static Observable<Integer> takeObserver(){
        return Observable.just(1,2,3,4).take(2);
    }
}
