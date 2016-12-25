package com.netflix.hystrix.contrib.util;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.integration.hystrix.CircuitBreakerFallbackMethodMissing;
import org.springframework.integration.hystrix.CircuitBreakerTimeoutException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

/**
 * 用来调用降级方法的工具类，主要目的就是找到降级的方法，然后执行这个降级的方法，原理就是利用反射去做。
 * Created by maluyong on 2016/12/25.
 */
public class ExceptionUtil {


    //用来执行降级方法的工具类
    public static Object handleException(HystrixRuntimeException e, ProceedingJoinPoint joinPoint,
                                   com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb) throws Throwable {
        if (cb.fallbackMethod().length() > 0) {
            return executeFallback(e, joinPoint, cb);
        }
        if (e.getCause() instanceof TimeoutException) {
            throw new CircuitBreakerTimeoutException();
        }
        if (e.getCause() != null) {
            throw e.getCause();
        }
        throw e;
    }
    public static Object executeFallback(HystrixRuntimeException e, ProceedingJoinPoint joinPoint,
                                   com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = AnnotationUtil.getMethod(joinPoint);
        Class<?> clazz = method.getDeclaringClass();
        String name = cb.fallbackMethod();
        Class<?> params[] = method.getParameterTypes();
        Object[] args = joinPoint.getArgs();

        Method m = ReflectionUtils.findMethod(clazz, name, params);
        if (m == null) {
            Class<?>[] temp = params;
            params = new Class<?>[params.length + 1];
            System.arraycopy(temp, 0, params, 0, temp.length);
            params[params.length - 1] = Throwable.class;

            Object[] tempArgs = args;
            args = new Object[tempArgs.length + 1];
            System.arraycopy(tempArgs, 0, args, 0, tempArgs.length);
            args[args.length - 1] = e.getCause() == null ? e : e.getCause();

            m = ReflectionUtils.findMethod(clazz, name, params);
        }
        if (m == null) {
            throw new CircuitBreakerFallbackMethodMissing(clazz, name, params);
        }
        return m.invoke(joinPoint.getTarget(), args);
    }

}
