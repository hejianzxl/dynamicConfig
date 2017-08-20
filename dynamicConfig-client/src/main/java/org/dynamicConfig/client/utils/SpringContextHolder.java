package org.dynamicConfig.client.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

public class SpringContextHolder implements ApplicationContextAware {
    
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.context = applicationContext;
    }
    
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return (T) context.getBean(clazz);
    }
    
    public static Object getBean(String name) {
        checkApplicationContext();
        return context.getBean(name);
    }
    
    public static ApplicationContext getContext() {
        return context;
    }

    private static void checkApplicationContext() {
        if(context == null) {
            throw new IllegalStateException("context is null.");
        }
    }

}
