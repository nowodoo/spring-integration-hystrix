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

	//������������ڲ�����ֻҪ�Ǵ������ע��ģ�����������á�
	@Around("@annotation(com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand)")
	public Object circuitBreakerAround(final ProceedingJoinPoint joinPoint) throws Throwable {
		com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb = AnnotationUtil.getAnnotation(joinPoint);
		try {

			//��Ҫ���þ��ǽ������ͷ��������ע������ںϣ������ϵ����á�
			return getHystrixCommand(joinPoint, cb).execute();
		} catch (HystrixRuntimeException e) {

			//�����ߵ��쳣����ע������ý��н���������������һЩ������  ��˵һ�䣬�����ע�⴫�ݽ�����ʱ����ʵ�Ǵ����˺ܶ����Ϣ���ġ�
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


				//���������Ȼ�������׳��쳣�ġ�
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

