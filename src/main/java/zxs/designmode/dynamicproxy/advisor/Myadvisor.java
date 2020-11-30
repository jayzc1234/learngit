package zxs.designmode.dynamicproxy.advisor;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;

public class Myadvisor implements Advisor{

	private Advice advice;
	
	public Myadvisor(Advice advice) {
		super();
		this.advice = advice;
	}

	@Override
	public Advice getAdvice() {
		return advice;
	}

	@Override
	public boolean isPerInstance() {
		return false;
	}

}
