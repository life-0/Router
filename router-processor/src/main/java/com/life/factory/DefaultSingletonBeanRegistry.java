package com.life.factory;

import java.util.HashMap;
import java.util.Map;

/*
 *@Author: life-0
 *@ClassName: DefaultSingletonBeanRegistry
 *@Date: 2023/9/7 11:16
 *TODO @Description:

 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {
    private Map<String, Object> singletonObjects = new HashMap<>();

    @Override
    public Object getSingleton(String name) {
        return singletonObjects.get(name);
    }

    /**
     * 在 DefaultSingletonBeanRegistry 中主要实现 getSingleton 方法，
     * 同时实现了一个受保护的 addSingleton 方法，这个方法可以被继承此类的其他类调用。
     * 包括：AbstractBeanFactory 以及继承的 DefaultListableBeanFactory 调用。
     *
     * @param beanName
     * @param singletonObject
     */
    protected void addSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
    }

}