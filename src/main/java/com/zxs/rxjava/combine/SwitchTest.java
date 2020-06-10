package com.zxs.rxjava.combine;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author zc
 */
public class SwitchTest {

    public static void main(String[] args) throws IOException {
        switchObserver().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        });
        System.in.read();
    }

    /**
     * take函数控制循环次数
     * @param index
     * @return
     */
    private static Observable<String> createObserver(long index){
        return Observable.interval(1000,1000, TimeUnit.MILLISECONDS).take(5).map(new Func1<Long, String>() {
            @Override
            public String call(Long aLong) {
                return "index:"+aLong;
            }
        });
    }

    /**
     * 注意这里的6000毫秒比createObserver方法的1000大了6倍
     * 是为了保证在switchObserver下一次循环时createObserver的一个5次循环已经结束了
     * 这样就能接收到5次循环所有的数据
     * 如果switchObserver的时间比createObserver的短，那么在switchObserver下次循环时createObserver的一次循环还
     * 没结束，就会导致还未发送的数据不会再发送
     * @return
     */
    private static Observable<String> switchObserver(){
        return Observable.switchOnNext(Observable.interval(6000,TimeUnit.MILLISECONDS).take(5)
                                                 .map(new Func1<Long, Observable<? extends String>>() {
                                                     @Override
                                                     public Observable<? extends String> call(Long aLong) {
                                                         return createObserver(aLong);
                                                     }
                                                 }));
    }
}
