package zxs.rxjava;

import rx.Single;
import rx.SingleSubscriber;

/**
 * 测试
 * @author zc
 */
public class SingleObservableTest {
    public static void main(String[] args) {
        Single.create(new Single.OnSubscribe<Integer>(){
            @Override
            public void call(SingleSubscriber<? super Integer> singleSubscriber) {
               if (!singleSubscriber.isUnsubscribed()){
                   singleSubscriber.onSuccess(1);
               }
            }
        }).subscribe(new SingleSubscriber<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                System.out.println("onSuccess:"+integer);
            }

            @Override
            public void onError(Throwable error) {
                System.out.println("onError");
            }
        });
    }
}
