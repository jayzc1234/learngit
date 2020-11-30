package zxs.test.concurrent;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlCallable;
import zxs.pojo.Person;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadLocalTest {

    private ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>(){
        @Override
        protected Integer initialValue() {
            return 1;
        }
    };

    private static int THREAD_COUNTER =1;
    private static ExecutorService executorService = Executors.newFixedThreadPool(1);

//    static {
//        executorService.execute(()->{
//            System.out.println("预热生成新线程");
//        });
//    }
    public static void main(String[] args) throws IOException, InterruptedException {
        test2();
    }

    private static void test2() throws InterruptedException, IOException {
        TransmittableThreadLocal<String> context = new TransmittableThreadLocal<String>();
        context.set("value-set-in-parent");

        Callable call = new Callable() {
            @Override
            public Object call() throws Exception {
                String s = context.get();
                System.out.println(s);
                return 1;
            }
        };
// 额外的处理，生成修饰了的对象ttlCallable
        Callable ttlCallable = TtlCallable.get(call);
        executorService.submit(ttlCallable);
    }

    private static void test1() throws IOException, InterruptedException {
        InheritableThreadLocal<Long> inheritableThreadLocal=new InheritableThreadLocal<>();
        inheritableThreadLocal.set(2L);

        ThreadLocal<Long> threadLocal=new ThreadLocal<>();
        threadLocal.set(1L);

        Person person=new Person();
        InheritableThreadLocal<Person> personInheritableThreadLocal=new InheritableThreadLocal<>();
        personInheritableThreadLocal.set(person);

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                Person person1 = personInheritableThreadLocal.get();
                System.out.println("new thread :"+person1.getId());
            }
        });
        person.setId(12);
        thread.start();

        executorService.execute(()->{
            Long aLong = inheritableThreadLocal.get();
            System.out.println(Thread.currentThread().getName()+" "+aLong);
        });

        Thread.sleep(1000);
        inheritableThreadLocal.set(22L);
        executorService.execute(()->{
            Long aLong = inheritableThreadLocal.get();
            System.out.println(Thread.currentThread().getName()+" "+aLong);
        });
        System.out.println("主线程结束");
        System.in.read();
    }
}
