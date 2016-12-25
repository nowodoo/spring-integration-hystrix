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

	//这个是切面的入口操作
	@Around("@annotation(com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand)")
	public Object circuitBreakerAround(final ProceedingJoinPoint joinPoint) throws Throwable {
		com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb = AnnotationUtil.getAnnotation(joinPoint);
		try {

			//主要作用就是将方法和方法上面的注解进行融合，产生断点配置。
			return getHystrixCommand(joinPoint, cb).execute();
		} catch (HystrixRuntimeException e) {
			return ExceptionUtil.handleException(e, joinPoint, cb);
		}
	}


	//这里是执行command命令的地方，主要的切入点
	private HystrixCommand<?> getHystrixCommand(final ProceedingJoinPoint joinPoint,
			com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb)
			throws NoSuchMethodException, SecurityException {

		@SuppressWarnings("rawtypes")
		HystrixCommand<?> theCommand = new HystrixCommand(ConfigUtil.getCommandSetter(joinPoint, cb)) {
			@Override
			protected Object run() throws Exception {
				try {

					//从这里可以看出就是这个方法的内部包装了 实际执行的方法
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

