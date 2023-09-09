package com.life.factory;

import com.life.bean.BeanDefinition;
import com.life.exception.BeansException;

import java.util.HashMap;
import java.util.Map;

/*
 *@Author: life-0
 *@ClassName: DefaultListableBeanFactory
 *@Date: 2023/9/7 13:39
 *TODO @Description:

 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements BeanDefinitionRegistry {

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) throw new BeansException("No bean named '" + beanName + "' is defined");
        return beanDefinition;
    }
}