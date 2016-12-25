package org.springframework.integration.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@org.springframework.stereotype.Service("hystrixCommandServiceImpl")
public class HystrixCommandServiceImpl implements Service {

	public static final int TEST_TIMEOUT = 2000;

	/**
	 *
	 *
	 * 配置线程池大小,默认值10个. 建议值:请求高峰时99.5%的平均响应时间 + 向上预留一些即可
	 *
	 * HystrixThreadPoolProperties.Setter().withCoreSize(int value)
	 * 配置线程值等待队列长度,默认值:-1 建议值:-1表示不等待直接拒绝,测试表明线程池使用直接决绝策略+
	 * 合适大小的非回缩线程池效率最高.所以不建议修改此值。
	 * 当使用非回缩线程池时，queueSizeRejectionThreshold,keepAliveTimeMinutes 参数无效
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
	@HystrixCommand(commandProperties = {@HystrixProperty(name = "executionTimeoutInMilliseconds", value = TEST_TIMEOUT + "") })
	public String withTimeout(String str) {
		try {
			Thread.sleep(2 * TEST_TIMEOUT);
		} catch (InterruptedException e) {
		}
		return str;
	}
	@Override
	@HystrixCommand(commandProperties = {@HystrixProperty(name = "executionTimeoutInMilliseconds", value = TEST_TIMEOUT + "") }, fallbackMethod = "fallbackWithException")
	public String withTimeoutAndFallback(String str) {
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
		System.out.println("enter fallbackMethod:" + "fallback(String s)");
		return s;
	}
	public Throwable fallbackWithException(String testStr, Throwable t) {
		System.out.println("enter fallbackMethod:" + "fallbackWithException(String testStr, Throwable t)");
		return t;
	}
}
