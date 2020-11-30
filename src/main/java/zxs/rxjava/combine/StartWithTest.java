package zxs.rxjava.combine;

import rx.Observable;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.List;

/**
 * 在源observable发送的数据前面插入一些数据
 * @author zc
 */
public class StartWithTest {

    public static void main(String[] args) {
        startWithObserver3().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
    private static Observable<Integer> startWithObserver(){
        return Observable.just(1,2,3).startWith(-1,-2);
    }

    private static Observable<Integer> startWithObserver2(){
        return Observable.just(1,2,3).startWith(Observable.just(-1));
    }

    private static Observable<Integer> startWithObserver3(){
        List<Integer> list=new ArrayList<>();
        list.add(-3);
        return Observable.just(1,2,3).startWith(list);
    }
}
