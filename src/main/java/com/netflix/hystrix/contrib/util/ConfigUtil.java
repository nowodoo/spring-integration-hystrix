package com.netflix.hystrix.contrib.util;

import com.netflix.hystrix.*;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import jodd.bean.BeanUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 说明: 用来帮助获取配置
 * Created by maluyong on 2016/12/25.
 */
public class ConfigUtil {

    //下面这三项是用来辅助提取配置文件的，是辅助方法。 从hstrixCommandAspect方法中抽离出来的。
    public static String getHystrixGroupName(final ProceedingJoinPoint joinPoint,
                                       com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb) {
        String name = cb.groupKey().length() == 0 ? cb.commandKey() : cb.groupKey();
        return name.length() == 0 ? joinPoint.getSignature().toShortString() : name;
    }
    public static HystrixCommandProperties.Setter getHystrixCommandPropertiesSetter(
            com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand hystrixCommand) {
        HystrixCommandProperties.Setter commandPropertiesDefaults = HystrixCommandProperties.defaultSetter();

        if (hystrixCommand.commandProperties() == null || hystrixCommand.commandProperties().length == 0) {
            return commandPropertiesDefaults;
        }

        Map<String, Object> commandProperties = new HashMap<String, Object>();
        for (HystrixProperty commandProperty : hystrixCommand.commandProperties()) {
            commandProperties.put(commandProperty.name(), commandProperty.value());
            BeanUtil.setDeclaredProperty(commandPropertiesDefaults, commandProperty.name(),
                    commandProperty.value());
        }
        return commandPropertiesDefaults;
    }
    public static HystrixThreadPoolProperties.Setter getHystrixThreadPoolPropertiesSetter(
            com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand hystrixCommand) {
        HystrixThreadPoolProperties.Setter commandPropertiesDefaults = HystrixThreadPoolProperties.defaultSetter();

        if (hystrixCommand.threadPoolProperties() == null || hystrixCommand.threadPoolProperties().length == 0) {
            return commandPropertiesDefaults;
        }
        Map<String, Object> commandProperties = new HashMap<String, Object>();
        for (HystrixProperty commandProperty : hystrixCommand.threadPoolProperties()) {
            commandProperties.put(commandProperty.name(), commandProperty.value());
            BeanUtil.setDeclaredProperty(commandPropertiesDefaults, commandProperty.name(),
                    commandProperty.value());
        }
        return commandPropertiesDefaults;
    }

    /**
     * 断路器主要配置 参考 http://hot66hot.iteye.com/blog/2155036
     *
     * @param joinPoint
     * @param cb
     * @return
     */
    public static HystrixCommand.Setter getCommandSetter(ProceedingJoinPoint joinPoint,
                                                   com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand cb) {

        //这里取出相应的配置名称
        String name = ConfigUtil.getHystrixGroupName(joinPoint, cb);
        String groupKey = StringUtils.isEmpty(cb.groupKey()) ? name : cb.groupKey();
        String commandKey = StringUtils.isEmpty(cb.commandKey()) ? name : cb.commandKey();
        HystrixThreadPoolKey hystrixThreadPoolKey = StringUtils.isEmpty(cb.threadPoolKey()) ? null : HystrixThreadPoolKey.Factory.asKey(cb.threadPoolKey());
        HystrixCommandProperties.Setter commandPropertiesDefaults = ConfigUtil.getHystrixCommandPropertiesSetter(cb);
        HystrixThreadPoolProperties.Setter threadPoolPropertiesDefaults = ConfigUtil.getHystrixThreadPoolPropertiesSetter(cb);


        //这里实际的配置对象
        return HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey)).andThreadPoolKey(hystrixThreadPoolKey)
                .andCommandPropertiesDefaults(commandPropertiesDefaults)
                .andThreadPoolPropertiesDefaults(threadPoolPropertiesDefaults);
    }
}
