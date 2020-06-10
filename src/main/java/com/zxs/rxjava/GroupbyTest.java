package com.zxs.rxjava;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.GroupedObservable;

public class GroupbyTest {
    public static void main(String[] args) {
//        groupByStringObserver().subscribe(new Action1<GroupedObservable<Integer, String>>() {
//            @Override
//            public void call(GroupedObservable<Integer, String> groupedObservable) {
//              if (groupedObservable.getKey() == 0){
//                  groupedObservable.subscribe(new Action1<String>() {
//                      @Override
//                      public void call(String s) {
//                          System.out.println(s);
//                      }
//                  });
//              }
//            }
//        });

        groupSubscrible();
    }

    private static void groupSubscrible() {
        groupByObserver().subscribe(new Action1<GroupedObservable<Integer, Integer>>() {
            @Override
            public void call(GroupedObservable<Integer, Integer> groupedObservable) {
                groupedObservable.count().subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        System.out.println("key:"+groupedObservable.getKey()+" contains:"+integer+" numbers");
                    }
                });

//                groupedObservable.forEach(new Action1<Integer>() {
//                    @Override
//                    public void call(Integer integer) {
//                        System.out.println(integer);
//                    }
//                });
            }
        });
    }

    private static Observable<GroupedObservable<Integer,Integer>> groupByObserver(){
        return Observable.just(1,2,3,4,5,6,7,8,9).groupBy(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                return integer % 2;
            }
        });
    }

    private static Observable<GroupedObservable<Integer,String>> groupByStringObserver(){
        return Observable.just(1,2,3,4,5,6,7,8,9).groupBy(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                return integer % 2;
            }
        }, new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {
                return "groupByKeyValue:"+integer;
            }
        });
    }
}
