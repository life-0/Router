package com.life.bean;

/*
 *@Author: life-0
 *@ClassName: BeanDefinition
 *@Date: 2023/7/6 22:19
 *TODO @Description:  用于定义 Bean 实例化信息，现在的实现是以一个 Object 存放对象

 */


public class BeanDefinition {
    private Class beanClass;

    public BeanDefinition(Class beanClass) {
        this.beanClass = beanClass;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }
}
