package com.life.factory;

import com.life.bean.BeanDefinition;
import com.life.exception.BeansException;

/*
 *@Author: life-0
 *@ClassName: AbstractBeanFactory
 *@Date: 2023/9/7 11:14
 *TODO @Description:    抽象类定义模板方法(AbstractBeanFactory)

 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory {

    @Override
    public Object getBean(String name) throws BeansException {
        Object bean = getSingleton(name);
        if (bean != null) {
            return bean;
        }

        BeanDefinition beanDefinition = getBeanDefinition(name);
        return createBean(name, beanDefinition);    //单例创建
    }

    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException;

}