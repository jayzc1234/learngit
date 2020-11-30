package zxs.rxjava.onerror;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.IOException;

/**
 * 处理错误
 * @author zc
 */
public class OnErrorReturnTest {

    public static void main(String[] args) throws IOException {
        onErrorResumeNextObserver().subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e);
            }

            @Override
            public void onNext(String s) {
                System.out.println(s);
            }
        });
        System.in.read();
    }

    private static void test1() {
        onErrorReturnObserver().subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }
            @Override
            public void onError(Throwable e) {
                System.out.println(e);
            }
            @Override
            public void onNext(String s) {
                //这里不写任何实现的话啥都不会执行
                System.out.println(s);
            }
        });
    }

    /**
     * 有错误时用新的observable发送数据，不会处理异常
     * @return
     */
    private static Observable<String> onErrorResumeNextObserver(){
        return createObserver().onErrorResumeNext(Observable.just("7","8","9"));
    }

    private static Observable<String> onErrorReturnObserver(){
      return createObserver().onErrorReturn(new Func1<Throwable, String>() {
          @Override
          public String call(Throwable throwable) {
              return "报错啦";
          }
      });
    }
    private static Observable<String> createObserver(){
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                for(int i=0;i<5;i++){
                    if (i<4){
                        subscriber.onNext("onNext:"+i);
                    }else {
                        subscriber.onError(new Throwable("throw error"));
                    }
                }
            }
        });
    }
}
