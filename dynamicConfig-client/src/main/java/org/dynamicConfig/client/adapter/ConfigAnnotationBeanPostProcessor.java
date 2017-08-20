package org.dynamicConfig.client.adapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.dynamicConfig.client.annotation.CodeConfig;
import org.dynamicConfig.client.factory.ThreadNameFactory;
import org.dynamicConfig.client.listener.NotifyThread;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

/**
 * 
 * bean初始化处理动态配置
 * 
 * @author hejian
 *
 */
public class ConfigAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements ApplicationContextAware {

	private JedisCluster										jedisCluster;

	private String												appName;

	public static volatile ConcurrentMap<String, Set<String>>	GLOBALCACHE		= new ConcurrentHashMap<>();

	public static volatile ConcurrentMap<String, String>		CACHE_LISTENTER	= new ConcurrentHashMap<>();

	public static volatile ConcurrentMap<String, Method>		METHOD_MAP		= new ConcurrentHashMap<>();

	public static volatile ConcurrentMap<String, Object>	    METHOD_CACH     = new ConcurrentHashMap<>();

	private static ApplicationContext							applicationContext;

	static final Executor										executor		= Executors
			.newSingleThreadExecutor(ThreadNameFactory.createNameThreadFactory("dynamic_config"));

	public ConfigAnnotationBeanPostProcessor(JedisCluster jedisCluster) {
		this(jedisCluster, NameFactory.init().create());
	}

	public ConfigAnnotationBeanPostProcessor(JedisCluster jedisCluster, String appName) {
		super();
		this.jedisCluster = jedisCluster;
	}

	/**
	 * bean实例化后调用
	 */
	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) {
		// 处理 annntation注解
		try {
			this.initializeField(bean, beanName);
			this.methodHandler(bean, beanName);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * @param bean
	 * @param beanName
	 */
	private void methodHandler(Object bean, String beanName) {
		Method[] methods = bean.getClass().getDeclaredMethods();
		if (StringUtils.isEmpty(methods)) {
			return;
		}
		//Reflections reflections = new Reflections();
		for (Method method : methods) {
			Annotation[][] annotations = method.getParameterAnnotations();
			for(Annotation[] aArray: annotations) {
				for(Annotation a: aArray) {
					if(a instanceof CodeConfig) {
						CodeConfig codeConfig = (CodeConfig)a;
						if (null != codeConfig) {
							ReflectionUtils.makeAccessible(method);
							try {
								method.invoke(bean, jedisCluster.get(codeConfig.key()));
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
							// 方法对象
							METHOD_MAP.putIfAbsent(codeConfig.groupID(), method);
						}
					}
				}
			}
		}
		
		if(!METHOD_MAP.isEmpty()){
			METHOD_CACH.put(beanName, METHOD_MAP);
		}
	}

	private void initializeField(Object bean, String beanName) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = bean.getClass().getDeclaredFields();
		if (StringUtils.isEmpty(fields)) {
			return;
		}

		for (Field field : fields) {
			CodeConfig codeConfig = field.getAnnotation(CodeConfig.class);
			if (null != codeConfig) {
				// REDIS初始化数据
				String groupId = codeConfig.groupID();
				String key = codeConfig.key();
				String value = jedisCluster.get(key);
				if (StringUtils.isEmpty(value)) {
					value = codeConfig.defaultValue();
				}

				ReflectionUtils.makeAccessible(field);
				field.set(bean, value);
				Set<String> beanNameSet = GLOBALCACHE.getOrDefault(key, Collections.emptySet());
				if (beanNameSet.isEmpty()) {
					beanNameSet = new HashSet<>();
				}

				beanNameSet.add(beanName);
				System.out.println("=============add key===============" + key);
				GLOBALCACHE.put(key, beanNameSet);
				// 注册监听
				if (!CACHE_LISTENTER.containsKey(groupId)) {
					executor.execute(new NotifyThread(groupId, jedisCluster, applicationContext));
				}
			}
		}
	}

	// 初始
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return super.postProcessBeforeInitialization(bean, beanName);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ConfigAnnotationBeanPostProcessor.applicationContext = applicationContext;
	}

}
