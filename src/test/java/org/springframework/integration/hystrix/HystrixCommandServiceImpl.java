package org.springframework.integration.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@org.springframework.stereotype.Service("hystrixCommandServiceImpl")
public class HystrixCommandServiceImpl implements Service {

	public static final int TEST_TIMEOUT = 2000;

	/**
	 * 
	 * 
	 * �����̳߳ش�С,Ĭ��ֵ10��. ����ֵ:����߷�ʱ99.5%��ƽ����Ӧʱ�� + ����Ԥ��һЩ����
	 *
	 * HystrixThreadPoolProperties.Setter().withCoreSize(int value)
	 * �����߳�ֵ�ȴ����г���,Ĭ��ֵ:-1 ����ֵ:-1��ʾ���ȴ�ֱ�Ӿܾ�,���Ա����̳߳�ʹ��ֱ�Ӿ�������+
	 * ���ʴ�С�ķǻ����̳߳�Ч�����.���Բ������޸Ĵ�ֵ��
	 * ��ʹ�÷ǻ����̳߳�ʱ��queueSizeRejectionThreshold,keepAliveTimeMinutes ������Ч
	 * HystrixThreadPoolProperties.Setter().withMaxQueueSize(int value)
	 */
	@Override
	@HystrixCommand(threadPoolProperties = { @HystrixProperty(name = "coreSize", value = "50"),
			@HystrixProperty(name = "maxQueueSize", value = "-1") })
	public String get(String str) {
		return str;
	}

	@Override
	@HystrixCommand
	public String throwException() throws MyException {
		throw new MyException();
	}


	//超时的方法
	@Override
	@HystrixCommand(commandProperties = {
			@HystrixProperty(name = "executionTimeoutInMilliseconds", value = TEST_TIMEOUT + "") })
	public String withTimeout(String str) {
		try {
			Thread.sleep(2 * TEST_TIMEOUT);
		} catch (InterruptedException e) {
		}
		return str;
	}
	@Override
	@HystrixCommand(commandProperties = { @HystrixProperty(name = "executionTimeoutInMilliseconds", value = "0") })
	public String withZeroTimeout(String str) {
		try {
			Thread.sleep(2 * TEST_TIMEOUT);
		} catch (InterruptedException e) {
		}
		return str;
	}


	//这两个是隔离方法的测试，到底是线程池隔离还是信号量隔离
	@Override
	@HystrixCommand(commandProperties = { @HystrixProperty(name = "executionIsolationStrategy", value = "THREAD") })
	public int getThreadId() {
		return Thread.currentThread().hashCode();
	}
	@Override
	@HystrixCommand(commandProperties = { @HystrixProperty(name = "executionIsolationStrategy.", value = "SEMAPHORE") })
	public int getNonThreadedThreadThreadId() {
		return Thread.currentThread().hashCode();
	}


	//降级的方法
	@Override
	@HystrixCommand(fallbackMethod = "fallback")
	public String exceptionWithFallback(String s) {
		throw new MyRuntimeException();
	}
	@Override
	@HystrixCommand(fallbackMethod = "fallbackWithException")
	public Throwable exceptionWithFallbackIncludingException(String testStr) {
		throw new MyRuntimeException();
	}


	//实际执行的讲解的方法，将会通过反射调用。
	public String fallback(String s) {
		return s;
	}
	public Throwable fallbackWithException(String testStr, Throwable t) {
		return t;
	}
}
