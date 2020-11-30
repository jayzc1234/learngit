package zxs.rxjava;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

public class RepeatAndTimerTest {

    private static Observable<Long> timerObservable(){
       return Observable.timer(1, TimeUnit.SECONDS).subscribeOn(Schedulers.io());
    }
}
