package com.life.factory;

import com.life.exception.BeansException;

public interface BeanFactory {
    Object getBean(String name) throws BeansException;
}
