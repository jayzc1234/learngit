package zxs.rxjava.model;

import rx.Observable;
import rx.functions.Action1;

public class CastObservableTest {

    public static void main(String[] args) {
        castObserver().subscribe(new Action1<Dog>() {
            @Override
            public void call(Dog dog) {
                System.out.println(dog.getName());
            }
        });
    }
   private static Observable<Dog> castObserver(){
      return Observable.just(getAnimal()).cast(Dog.class);
   }

   private static Animal getAnimal(){
        return new Dog();
   }
}
