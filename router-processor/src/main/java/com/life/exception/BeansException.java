package com.life.exception;

import com.life.processor_utils.RouterLog;

/*
 *@Author: life-0
 *@ClassName: BeanException
 *@Date: 2023/9/6 19:29
 *TODO @Description:

 */
public class BeansException extends Exception {

    public BeansException(String message) {
        super(message);
        RouterLog.e(message);
    }

    public BeansException(String instantiationOfBeanFailed, ReflectiveOperationException e) {
        RouterLog.e(instantiationOfBeanFailed);
        RouterLog.e(e.toString());
    }
}

