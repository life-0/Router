package com.life.factory;/*
 *@Author: life-0
 *@ClassName: BeanDefinitionRegistry
 *@Date: 2023/7/6 23:20
 *TODO @Description:

 */


import com.life.bean.BeanDefinition;

public interface BeanDefinitionRegistry {
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);
}
