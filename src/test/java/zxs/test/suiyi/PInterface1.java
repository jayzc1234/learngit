package zxs.test.suiyi;

public interface PInterface1 {
    default void print(String name){
        System.out.println("PInterface1:"+name);
    }
}
