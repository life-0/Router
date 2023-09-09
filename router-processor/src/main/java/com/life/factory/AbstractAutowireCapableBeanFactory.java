package com.life.factory;

import com.life.bean.BeanDefinition;
import com.life.exception.BeansException;
import com.life.processor_utils.RouterLog;

/*
 *@Author: life-0
 *@ClassName: AbstractAutowireCapableBeanFactory
 *@Date: 2023/9/7 11:09
 *TODO @Description:

 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {

    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition) {
        Object bean = null;
        try {
            bean = beanDefinition.getBeanClass().newInstance();// Fragment不需要带参实例化
        } catch (InstantiationException | IllegalAccessException e) {
            RouterLog.e("Instantiation of bean failed");
            e.printStackTrace();
        }

        addSingleton(beanName, bean);
        return bean;
    }

}