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

@Aspect
@Component
public class HystrixCommandAspect {

	//这个是切面的入口操作，只要是带有这个注解的，都会产生作用。
	@Around("@annotation(com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand)")
	public Object circuitBreakerAround(final ProceedingJoinPoint joinPoint) throws Throwable {
		com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb = AnnotationUtil.getAnnotation(joinPoint);
		try {

			//主要作用就是将方法和方法上面的注解进行融合，产生断点配置。
			return getHystrixCommand(joinPoint, cb).execute();
		} catch (HystrixRuntimeException e) {

			//将主线的异常根据注解的配置进行降级或者是其他的一些操作。  多说一句，将这个注解传递进来的时候，其实是传递了很多的信息量的。
			return ExceptionUtil.handleException(e, joinPoint, cb);
		}
	}


	//这里是执行command命令的地方，主要的切入点
	private HystrixCommand<?> getHystrixCommand(final ProceedingJoinPoint joinPoint, com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb) throws NoSuchMethodException, SecurityException {

		//获取需要的配置
		HystrixCommand.Setter setter = ConfigUtil.getCommandSetter(joinPoint, cb);


		//用上面的配置去建立真正的执行策略，超时，异常等等
		@SuppressWarnings("rawtypes")
		HystrixCommand<?> theCommand = new HystrixCommand(setter) {
			@Override
			protected Object run() throws Exception {
				try {

					//从这里可以看出就是这个方法的内部包装了 实际执行的方法
					return joinPoint.proceed();


				//这个方法仍然是往外抛出异常的。
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

