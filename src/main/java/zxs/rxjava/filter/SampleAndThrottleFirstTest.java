package zxs.rxjava.filter;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * sample是取规定时间段内最后一个数据，而throttleFirst操作符取的则是规定时间段内的
 * 第一个数据
 * @author zc
 */
public class SampleAndThrottleFirstTest {

    public static void main(String[] args) throws IOException {

//        Observable.interval(1, TimeUnit.MICROSECONDS).sample(2, TimeUnit.SECONDS).subscribe(System.out::println);
        sampleObserver().subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                System.out.println(aLong);
            }
        });
        int read = System.in.read();
    }

    private static Observable<Long> sampleObserver(){
       return Observable.interval(200, TimeUnit.MILLISECONDS).sample(1000,TimeUnit.MILLISECONDS);
    }


}
