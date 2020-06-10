//package com.zxs.designmode.dynamicproxy.cglib;
//
//import java.lang.reflect.Method;
//
//import com.zxs.designmode.dynamicproxy.LoveImp;
//
//import net.sf.cglib.proxy.Enhancer;
//import net.sf.cglib.proxy.MethodInterceptor;
//import net.sf.cglib.proxy.MethodProxy;
//
//public class MyMethodInterceptor implements MethodInterceptor{
//
//   private LoveImp loveImp;
//    // 根据一个类型产生代理类，此方法不要求一定放在MethodInterceptor中
//    public Object CreatProxyedObj(Class<?> clazz)
//    {
//        Enhancer enhancer = new Enhancer();
//
//        enhancer.setSuperclass(clazz);
//
//        enhancer.setCallback(this);
//
//        return enhancer.create();
//    }
//
//	@Override
//	public Object intercept(Object obj, Method arg1, Object[] args, MethodProxy mProxy) throws Throwable {
//		System.out.println("动态代理执行了方法："+arg1.getName());
//		Object o=mProxy.invoke(this.loveImp, args);
//		return o;
//	}
//
//	public static void main(String[] args) {
//		MyMethodInterceptor m=new MyMethodInterceptor();
//		LoveImp loveImp=new LoveImp();
//		m.loveImp=loveImp;
//
//		LoveImp loveImp2=(LoveImp) m.CreatProxyedObj(LoveImp.class);
//		loveImp2.whoisLover("fjl");
//	}
//}
