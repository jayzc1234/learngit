package zxs.test.objectoriented;

public class OriginalBean {

    public void testA(){
        System.out.println("OriginalBean testA");
    }

    public void testB(){
        System.out.println("OriginalBean testB");
        testA();
    }
}
