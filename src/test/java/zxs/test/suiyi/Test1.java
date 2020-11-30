package zxs.test.suiyi;

public class Test1 extends AbstractPInterface1 implements PInterface1 {

    public static void main(String[] args) {
        Test1 test1 = new Test1();
        test1.print("tt");
    }

    @Override
    public String toString() {
        System.out.println("执行Test1 tostring方法");
        return "Test1{}";
    }
}
