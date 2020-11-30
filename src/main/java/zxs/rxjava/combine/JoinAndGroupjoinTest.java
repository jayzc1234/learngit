package zxs.rxjava.combine;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * join
 * @author zc
 */
public class JoinAndGroupjoinTest {

    public static void main(String[] args) throws IOException {
        JoinAndGroupjoinTest joinAndGroupjoinTest=new JoinAndGroupjoinTest();
        joinAndGroupjoinTest.joinObserver().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        });
        System.in.read();
    }
    private Observable<Long> getLeftObservable(){
        return Observable.interval(500,TimeUnit.MILLISECONDS);
    }
    private Observable<Long> getRightObservable(){
        return Observable.just(1L,2L,3L);
    }

    /**
     * fun中定义的时间表示在该时间段内的数据有效
     * 由右边的observable组合到左边的observable，所以
     * 右边的时间间隔过了就不会执行了
     * @return
     */
    private Observable<String> joinObserver(){
        return getLeftObservable().join(getRightObservable(), new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long s) {
                return Observable.timer(2000000, TimeUnit.MILLISECONDS);
            }
        }, new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                return Observable.timer(150000,TimeUnit.MILLISECONDS);
            }
        }, new Func2<Long, Long, String>() {
            @Override
            public String call(Long left, Long right) {
                return left+":"+right;
            }
        });
    }
}
