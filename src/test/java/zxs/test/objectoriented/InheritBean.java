package zxs.test.objectoriented;

public class InheritBean extends OriginalBean{

    private OriginalBean originalBean;

    public InheritBean (OriginalBean originalBean){
        this.originalBean = originalBean;
    }

    @Override
    public void testA() {
        System.out.println("InheritBean testA");
        originalBean.testA();
    }

    @Override
    public void testB() {
        System.out.println("InheritBean testB");
        originalBean.testB();
    }

    public static void main(String[] args) {
        OriginalBean originalBean = new OriginalBean();
        InheritBean inheritBean = new InheritBean(originalBean);
        inheritBean.testB();
    }
}
