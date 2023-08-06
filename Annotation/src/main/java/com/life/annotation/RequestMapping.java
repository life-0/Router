package com.life.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RequestMapping {
    /**
     * 当前页面的url，不能为空
     *
     * @return 页面的url
     */
    String url();

    /**
     * 对于当前页面的描述
     *
     * @return 例如："个人主页"
     */
    String description() default "";

    /**
     * 承载子页面的视图id值
     */
    int container() default -1;
}