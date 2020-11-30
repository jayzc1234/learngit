package zxs.rxjava.filter;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 从源中找出第一个或者最后一个
 * @author zc
 */
public class FirstAndLastTest {
    public static void main(String[] args) {
        customFirst();
    }

    private static void first() {
        Observable.range(1,10).first().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }

    /**
     * 找到第一个就输出了
     */
    private static void customFirst() {
        Observable.range(1,10).first(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                return integer>5;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
    private static void last() {
        Observable.range(1,10).last().subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer);
            }
        });
    }
}
