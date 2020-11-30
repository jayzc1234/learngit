package zxs.test.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class ReflectMain {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        // 保存生成的代理类的字节码文件
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
        FlyI flyI = new FlyIImpl();
        FlyI flyI2 = (FlyI) Proxy.newProxyInstance(ReflectMain.class.getClassLoader(), new Class[]{FlyI.class}, new FlyInvocationHandler(flyI));
        flyI2.fly();
    }
}
