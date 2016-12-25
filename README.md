#说明
1.
用HystrixCommandTest 这个类当做controller去调用serviceImpl类,然而在servcieImpl上面是添加了熔断逻辑的。

2.
关于降级操作是利用反射直接调用某一个类的某一个方法，这个方法是定义在
@HystrixCommand(fallbackMethod = "fallbackWithException")
这个注解上面的。

3.
hystrix的初始化是在aspect里面进行初始化的。




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

###��������˵��
1:Hystrix ����
1):Command ����
Command����Դ����HystrixCommandProperties,����Commandʱͨ��Setter��������
�������ý��ͺ�Ĭ��ֵ����
Java����  �ղش���
//ʹ��������ø��뷽ʽ,Ĭ��:�����̸߳���,ExecutionIsolationStrategy.THREAD  
private final HystrixProperty<ExecutionIsolationStrategy> executionIsolationStrategy;   
//ʹ���̸߳���ʱ�����ó�ʱʱ�䣬Ĭ��:1��  
private final HystrixProperty<Integer> executionIsolationThreadTimeoutInMilliseconds;   
//�̳߳ص�key,���ھ����������ĸ��̳߳�ִ��  
private final HystrixProperty<String> executionIsolationThreadPoolKeyOverride;   
//ʹ���ź�������ʱ������������Ĳ�����,Ĭ��:10  
private final HystrixProperty<Integer> executionIsolationSemaphoreMaxConcurrentRequests;  
//ʹ���ź�������ʱ������fallback(����)�������Ĳ�����,Ĭ��:10  
private final HystrixProperty<Integer> fallbackIsolationSemaphoreMaxConcurrentRequests;   
//�Ƿ���fallback�������� Ĭ��:true   
private final HystrixProperty<Boolean> fallbackEnabled;   
// ʹ���̸߳���ʱ���Ƿ������ִ�г�ʱ���̵߳����жϣ�Thread.interrupt()������.Ĭ��:true  
private final HystrixProperty<Boolean> executionIsolationThreadInterruptOnTimeout;   
// ͳ�ƹ�����ʱ�䴰��,Ĭ��:5000����circuitBreakerSleepWindowInMilliseconds  
private final HystrixProperty<Integer> metricsRollingStatisticalWindowInMilliseconds;  
// ͳ�ƴ��ڵ�Buckets������,Ĭ��:10��,ÿ��һ��Bucketsͳ��  
private final HystrixProperty<Integer> metricsRollingStatisticalWindowBuckets; // number of buckets in the statisticalWindow  
//�Ƿ������ͳ�ƹ���,Ĭ��:true  
private final HystrixProperty<Boolean> metricsRollingPercentileEnabled;   
// �Ƿ���������־,Ĭ��:true  
private final HystrixProperty<Boolean> requestLogEnabled;   
//�Ƿ������󻺴�,Ĭ��:true  
private final HystrixProperty<Boolean> requestCacheEnabled; // Whether request caching is enabled.  
 
2):�۶�����Circuit Breaker������
Circuit Breaker����Դ����HystrixCommandProperties,����Commandʱͨ��Setter��������,ÿ������ʹ��һ��Circuit Breaker
Java����  �ղش���
// �۶���������ͳ��ʱ�����Ƿ����ķ�ֵ��Ĭ��20�롣Ҳ����10��������������20�Σ��۶����ŷ���������  
private final HystrixProperty<Integer> circuitBreakerRequestVolumeThreshold;   
//�۶���Ĭ�Ϲ���ʱ��,Ĭ��:5��.�۶����ж�����5���������״̬,�Ų���������ȥ����  
private final HystrixProperty<Integer> circuitBreakerSleepWindowInMilliseconds;   
//�Ƿ������۶���,Ĭ��true. ����  
private final HystrixProperty<Boolean> circuitBreakerEnabled;   
//Ĭ��:50%���������ʳ���50%���۶�������.  
private final HystrixProperty<Integer> circuitBreakerErrorThresholdPercentage;  
//�Ƿ�ǿ�ƿ����۶��������������,Ĭ��:false,������  
private final HystrixProperty<Boolean> circuitBreakerForceOpen;   
//�Ƿ������۶������Դ���,Ĭ��false, ������  
private final HystrixProperty<Boolean> circuitBreakerForceClosed;  
 
3):����ϲ�(Collapser)����
Command����Դ����HystrixCollapserProperties,����Collapserʱͨ��Setter��������
Java����  �ղش���
//����ϲ�����������������,Ĭ��: Integer.MAX_VALUE  
private final HystrixProperty<Integer> maxRequestsInBatch;  
//�����������ÿ�������ӳٵ�ʱ��,Ĭ��:10����  
private final HystrixProperty<Integer> timerDelayInMilliseconds;  
//������������Ƿ������󻺴�,Ĭ��:����  
private final HystrixProperty<Boolean> requestCacheEnabled;  
 
4):�̳߳�(ThreadPool)����
Java����  �ղش���
/** 
�����̳߳ش�С,Ĭ��ֵ10��. 
����ֵ:����߷�ʱ99.5%��ƽ����Ӧʱ�� + ����Ԥ��һЩ���� 
*/  
HystrixThreadPoolProperties.Setter().withCoreSize(int value)  
/** 
�����߳�ֵ�ȴ����г���,Ĭ��ֵ:-1 
����ֵ:-1��ʾ���ȴ�ֱ�Ӿܾ�,���Ա����̳߳�ʹ��ֱ�Ӿ�������+ ���ʴ�С�ķǻ����̳߳�Ч�����.���Բ������޸Ĵ�ֵ�� 
��ʹ�÷ǻ����̳߳�ʱ��queueSizeRejectionThreshold,keepAliveTimeMinutes ������Ч 
*/  
HystrixThreadPoolProperties.Setter().withMaxQueueSize(int value)  

