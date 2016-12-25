package org.springframework.integration.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:spring/applicationConfig.xml" })
public class HystrixCommandTest {

	private static final String TEST_STR = "TEST_STR";
	@Autowired
	@Qualifier(value = "hystrixCommandServiceImpl")
	private Service service;

	@Test
	public void testHystrix() {
		assertEquals(TEST_STR, service.get(TEST_STR));
	}


	//这个方法设置了超时策略
	@Test
	public void testTimeoutHystrix() {
		long start = System.currentTimeMillis();
		try {

			//hystrix本身设置了两秒超时，然后在方法里面实际睡了四秒,那么肯定是会抛出异常的
			service.withTimeout(TEST_STR);

		} catch (CircuitBreakerTimeoutException e) {

			//这里会执行到要是抛出异常的话
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();


		//确定时间范围肯定在规定的时间范围内
		assertTrue(end - start > HystrixCommandServiceImpl.TEST_TIMEOUT);
		assertTrue(end - start < HystrixCommandServiceImpl.TEST_TIMEOUT * 1.1f);
	}
	//这里不允许有任何的超时，就是请求了之后马上返回
	@Test
	public void testZeroTimeoutHystrix() {
		long start = System.currentTimeMillis();
		service.withZeroTimeout(TEST_STR);

		long end = System.currentTimeMillis();
		assertTrue(end - start > HystrixCommandServiceImpl.TEST_TIMEOUT * 2);
		assertTrue(end - start < HystrixCommandServiceImpl.TEST_TIMEOUT * 2.1f);
	}
	@Test
	public void withTimeoutAndFallback() {
		long start = System.currentTimeMillis();
		service.withTimeoutAndFallback(TEST_STR);

		long end = System.currentTimeMillis();
		assertTrue(end - start > HystrixCommandServiceImpl.TEST_TIMEOUT * 2);
		assertTrue(end - start < HystrixCommandServiceImpl.TEST_TIMEOUT * 2.1f);
	}


	//一个最简单的抛出异常
	@Test(expected = MyException.class)
	public void testException() throws MyException {
		service.throwException();
	}


	//测试hystrix和实际的情况 不是用的同一个线程 用新的线程隔离
	@Test
	public void testThreaded() throws MyException {
		int threadId = Thread.currentThread().hashCode();
		int serviceThreadId = service.getThreadId();

		assertNotEquals(threadId, serviceThreadId);
	}
	//这个说明调用的线程是来自hystrix的线程,但是这个是使用的信号量。
	@Test
	public void testNonThreaded() throws MyException {
		int threadId = Thread.currentThread().hashCode();
		System.out.println(service);
		int serviceThreadId = service.getNonThreadedThreadThreadId();

		assertEquals(threadId, serviceThreadId);
	}


	//测试降级的方法
	@Test
	public void testExceptionWithFallback() throws MyException {
		assertEquals(TEST_STR, service.exceptionWithFallback(TEST_STR));
	}
	@Test
	public void testExceptionPassingExceptionToFallback() throws MyException {
		Throwable t = service.exceptionWithFallbackIncludingException(TEST_STR);
		assertTrue(t instanceof MyRuntimeException);
	}


	//
	@Test
	public void testThreadPoolProperties() throws Exception {
		for (int i = 0; i < 10; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println(service.get(TEST_STR));
				}
			}).start();
			;

		}

		Thread.sleep(10000);
	}

}
