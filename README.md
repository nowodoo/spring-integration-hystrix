1. 用HystrixCommandTest 这个类当做controller去调用serviceImpl类,然而在servcieImpl上面是添加了熔断逻辑的。

2. 关于降级操作是利用反射直接调用某一个类的某一个方法，这个方法是定义在 @HystrixCommand(fallbackMethod = "fallbackWithException") 这个注解上面的。

3. hystrix的初始化是在aspect里面进行初始化的。

4. 流程介绍： 注意HystrixCommandAspect里面的getHystrixCommand()方法，实际就是包裹了执行的方法， 然后用getCommandSetter()这个方法获取实际的配置。 这些配置来自方法上面的注解




# CircuitBreaker
An Spring AOP annotation HystrixCommand using the NetFlix Hystrix circuit breaker (https://github.com/Netflix/Hystrix)
Use

    
    @HystrixCommand
    public String methodName() throws MyException {
    }
    
    @HystrixCommand(commandProperties = {@HystrixProperty(name="executionTimeoutInMilliseconds",value = "0") })
	public String withZeroTimeout(String str) {
	
	
	@HystrixCommand(threadPoolProperties = { @HystrixProperty(name = "coreSize", value = "50"),
			@HystrixProperty(name = "maxQueueSize", value = "-1") })
	public String get(String str) {
		return str;
	}

### Spring config
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:aop="http://www.springframework.org/schema/aop"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">

	<aop:aspectj-autoproxy/>
	<context:component-scan base-package="org.springframework.integration.hystrix"/>

</beans>

###参数配置说明
####1:Hystrix 配置:
//Command 配置 Command配置源码在HystrixCommandProperties,构造Command时通过Setter进行配置 具体配置解释和默认值如下 Java代码 收藏代码 //使用命令调用隔离方式,默认:采用线程隔离,ExecutionIsolationStrategy.THREAD
private final HystrixProperty executionIsolationStrategy;

//使用线程隔离时，调用超时时间，默认:1秒
private final HystrixProperty executionIsolationThreadTimeoutInMilliseconds;

//线程池的key,用于决定命令在哪个线程池执行
private final HystrixProperty executionIsolationThreadPoolKeyOverride;

//使用信号量隔离时，命令调用最大的并发数,默认:10
private final HystrixProperty executionIsolationSemaphoreMaxConcurrentRequests;

//使用信号量隔离时，命令fallback(降级)调用最大的并发数,默认:10
private final HystrixProperty fallbackIsolationSemaphoreMaxConcurrentRequests;

//是否开启fallback降级策略 默认:true
private final HystrixProperty fallbackEnabled;

// 使用线程隔离时，是否对命令执行超时的线程调用中断（Thread.interrupt()）操作.默认:true
private final HystrixProperty executionIsolationThreadInterruptOnTimeout;

// 统计滚动的时间窗口,默认:5000毫秒circuitBreakerSleepWindowInMilliseconds
private final HystrixProperty metricsRollingStatisticalWindowInMilliseconds;

// 统计窗口的Buckets的数量,默认:10个,每秒一个Buckets统计
private final HystrixProperty metricsRollingStatisticalWindowBuckets; // number of buckets in the statisticalWindow

//是否开启监控统计功能,默认:true
private final HystrixProperty metricsRollingPercentileEnabled;

// 是否开启请求日志,默认:true
private final HystrixProperty requestLogEnabled;

//是否开启请求缓存,默认:true
private final HystrixProperty requestCacheEnabled; // Whether request caching is enabled.

####2):熔断器（Circuit Breaker）
配置 Circuit Breaker配置源码在HystrixCommandProperties,构造Command时通过Setter进行配置,每种依赖使用一个Circuit Breaker Java代码 收藏代码 // 熔断器在整个统计时间内是否开启的阀值，默认20秒。也就是10秒钟内至少请求20次，熔断器才发挥起作用
private final HystrixProperty circuitBreakerRequestVolumeThreshold;

//熔断器默认工作时间,默认:5秒.熔断器中断请求5秒后会进入半打开状态,放部分流量过去重试
private final HystrixProperty circuitBreakerSleepWindowInMilliseconds;

//是否启用熔断器,默认true.
private final HystrixProperty circuitBreakerEnabled;

//默认:50%。当出错率超过50%后熔断器启动.
private final HystrixProperty circuitBreakerErrorThresholdPercentage;

//是否强制开启熔断器阻断所有请求,默认:false,不开启
private final HystrixProperty circuitBreakerForceOpen;

//是否允许熔断器忽略错误,默认false, 不开启
private final HystrixProperty circuitBreakerForceClosed;

####3):命令合并(Collapser)配置 
Command配置源码在HystrixCollapserProperties,构造Collapser时通过Setter进行配置 Java代码 收藏代码 //请求合并是允许的最大请求数,默认: Integer.MAX_VALUE
private final HystrixProperty maxRequestsInBatch;

//批处理过程中每个命令延迟的时间,默认:10毫秒
private final HystrixProperty timerDelayInMilliseconds;

//批处理过程中是否开启请求缓存,默认:开启
private final HystrixProperty requestCacheEnabled;

####4):线程池(ThreadPool)配置 
Java代码 收藏代码 /** 配置线程池大小,默认值10个. 建议值:请求高峰时99.5%的平均响应时间 + 向上预留一些即可 /
HystrixThreadPoolProperties.Setter().withCoreSize(int value)

/* 配置线程值等待队列长度,默认值:-1 建议值:-1表示不等待直接拒绝,测试表明线程池使用直接决绝策略+ 合适大小的非回缩线程池效率最高.所以不建议修改此值。 当使用非回缩线程池时，queueSizeRejectionThreshold,keepAliveTimeMinutes 参数无效 */
HystrixThreadPoolProperties.Setter().withMaxQueueSize(int value)

