package zxs.test.jvm;

import java.util.concurrent.TimeUnit;

public class TestClassLoading extends InitTest1{
    
    static {
        System.out.println("class TestClassLoading init");
    }


    public static class A{
        static{
            System.out.println("class A init");
            try {
                TimeUnit.SECONDS.sleep(1);
            }catch (Exception e){
                e.printStackTrace();
            }
            new B();
        }

        public static void test(){
            System.out.println("aaa");
        }
    }

    public static class B{
        static{
            System.out.println("class B init");
            try {
                TimeUnit.SECONDS.sleep(1);
            }catch (Exception e){
                e.printStackTrace();
            }
            new A();
        }
        public static void test(){
            System.out.println("BBB");
        }
    }

    public static void main(String[] args) {
        new Thread(() -> A.test()).start();
        new Thread(() -> B.test()).start();

    }
}
