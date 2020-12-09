package com.zxs.rxjava.combine;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 合并，数据不能重复合并
 * @author zc
 */
public class ZipTest {
    public static void main(String[] args) throws IOException {
        zipWithObserver().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        });

        System.in.read();
    }
    private static Observable<String> zipWithObserver(){
        return createObserver(2).zipWith(createObserver(3), new Func2<String, String, String>() {
            @Override
            public String call(String s, String s2) {
                return s+"-"+s2;
            }
        });
    }

    private static Observable<String>createObserver(int index){
      return Observable.interval(1000, TimeUnit.MILLISECONDS).take(index).map(new Func1<Long, String>() {
          @Override
          public String call(Long aLong) {
              return index+":"+aLong;
          }
      });
    }
}
