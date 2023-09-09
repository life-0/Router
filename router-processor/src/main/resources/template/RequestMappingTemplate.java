package com.life.router.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.life.bean.ViewBean;
import com.life.bean.ViewType;
import com.life.processor_utils.ObjectUtils;
import com.life.processor_utils.UrlUtil;

public class RequestMapping {
    private static HashMap<String, ViewBean> mapping = new HashMap<>();

    /**
     * 初始化 并创建ViewBean
     */
    public RequestMapping() {
        /*  example:
        ArrayList<String> urlList = UrlUtil.getInstance().splitUrl("/index_2/demo_fragment");
        ViewBean viewBean = new ViewBean("/index_2/demo_fragment", ViewType.FRAGMENT,
                "com.life.app.view.fragment.PageFragment1",
                -1,
                "第二子页",
                new HashMap<String, ViewBean>());
        generateViewBean(urlList, mapping, viewBean);
        */

    }

    public static HashMap<String, ViewBean> get() {
        return mapping;
    }

    /**
     * 生成ViewBean
     *
     * @param url
     * @return
     */
    private void generateViewBean(ArrayList<String> list, HashMap<String, ViewBean> childrenViewBeanMap, ViewBean targetViewBean) {
        String url = list.remove(0);
        ViewBean bean = childrenViewBeanMap.get(url);
        HashMap<String, ViewBean> children = ObjectUtils.isNotEmpty(bean) ? bean.getChildren() : null;
        //判断当前的url是否为最后一个
        if (list.size() == 0) {
            //子页面存在, 只更新数据, 不覆盖子页面数据
            if (ObjectUtils.isNotEmpty(children)) {
                bean.setUrl(targetViewBean.getUrl());
                bean.setViewType(targetViewBean.getViewType());
                bean.setClassName(targetViewBean.getClassName());
                bean.setContainerId(targetViewBean.getContainerId());
                bean.setDescription(targetViewBean.getDescription());
                childrenViewBeanMap.put(url, bean);
                return;
            } else {
                //子页面不存在,直接创建目标页面数据
                bean = new ViewBean(targetViewBean.getUrl(), targetViewBean.getViewType(),
                        targetViewBean.getClassName(), targetViewBean.getContainerId(),
                        targetViewBean.getDescription(), new HashMap<String, ViewBean>());
                childrenViewBeanMap.put(url, bean);
                return;
            }
        }

        if (ObjectUtils.isNotEmpty(bean)) { //不为空就获取子页面集合
            generateViewBean(list, children, targetViewBean);   //走递归
        } else {    //不存在就创建子页面
            ViewBean viewBean = new ViewBean(url, ViewType.DEFAULT, "null", -1,
                    "null", new HashMap<String, ViewBean>());
            mapping.put(url, viewBean);
            generateViewBean(list, viewBean.getChildren(), targetViewBean);   //走递归
        }
    }
}