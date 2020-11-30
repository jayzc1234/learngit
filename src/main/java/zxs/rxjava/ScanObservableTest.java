package zxs.rxjava;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zc
 */
public class ScanObservableTest {

    public static void main(String[] args) {
        scanObservable().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println("scan:"+ integer);
            }
        });
    }
    private static Observable<Integer> scanObservable(){
        List<Integer> list=new ArrayList<>();
        list.add(2);
        list.add(2);
        list.add(2);
        return Observable.from(list).scan(new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) {
                return integer * integer2;
            }
        });
    }
}
