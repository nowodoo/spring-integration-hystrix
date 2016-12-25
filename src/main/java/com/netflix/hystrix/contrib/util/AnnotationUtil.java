package com.netflix.hystrix.contrib.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 获取主耳机相关的配置类
 * Created by maluyong on 2016/12/25.
 */
public class AnnotationUtil {

    //用来获取方法
    public static com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand getAnnotation(
            final ProceedingJoinPoint joinPoint) {
        Method method = getMethod(joinPoint);
        return method.getAnnotation(com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand.class);
    }

    //根据获取到的方法，然后来获取注解
    public static Method getMethod(final ProceedingJoinPoint joinPoint) {
        try {
            final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            if (method.getDeclaringClass().isInterface()) {
                final String methodName = joinPoint.getSignature().getName();
                method = joinPoint.getTarget().getClass().getDeclaredMethod(methodName, method.getParameterTypes());
            }
            return method;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
