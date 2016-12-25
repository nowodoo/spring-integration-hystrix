package org.springframework.integration.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.contrib.util.AnnotationUtil;
import com.netflix.hystrix.contrib.util.ConfigUtil;
import com.netflix.hystrix.contrib.util.ExceptionUtil;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

@Aspect
@Component
public class HystrixCommandAspect {

	//������������ڲ���
	@Around("@annotation(com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand)")
	public Object circuitBreakerAround(final ProceedingJoinPoint joinPoint) throws Throwable {
		com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb = AnnotationUtil.getAnnotation(joinPoint);
		try {

			//��Ҫ���þ��ǽ������ͷ��������ע������ںϣ������ϵ����á�
			return getHystrixCommand(joinPoint, cb).execute();
		} catch (HystrixRuntimeException e) {
			return ExceptionUtil.handleException(e, joinPoint, cb);
		}
	}


	//������ִ��command����ĵط�����Ҫ�������
	private HystrixCommand<?> getHystrixCommand(final ProceedingJoinPoint joinPoint,
			com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb)
			throws NoSuchMethodException, SecurityException {

		@SuppressWarnings("rawtypes")
		HystrixCommand<?> theCommand = new HystrixCommand(ConfigUtil.getCommandSetter(joinPoint, cb)) {
			@Override
			protected Object run() throws Exception {
				try {

					//��������Կ�����������������ڲ���װ�� ʵ��ִ�еķ���
					return joinPoint.proceed();
				} catch (Exception e) {
					throw e;
				} catch (Throwable e) {
					throw new Exception(e);
				}
			}
		};
		return theCommand;
	}


}

