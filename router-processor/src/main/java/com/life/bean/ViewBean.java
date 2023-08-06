package com.life.bean;

import java.util.HashMap;

public class ViewBean {
    private String url; //链接
    private ViewType viewType;
    private String className;   //类名
    private int containerId;    //容器id
    private String description; //描述
    private HashMap<String, ViewBean> children; //子页面

    public ViewBean(String url, ViewType viewType, String className,
                    int containerId, String description, HashMap<String, ViewBean> children) {
        this.url = url;
        this.viewType = viewType;
        this.className = className;
        this.containerId = containerId;
        this.description = description;
        this.children = children;
    }

    public ViewBean(String url, ViewType viewType, String className, int containerId, String description) {
        this.url = url;
        this.viewType = viewType;
        this.className = className;
        this.containerId = containerId;
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ViewType getViewType() {
        return viewType;
    }

    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getContainerId() {
        return containerId;
    }

    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, ViewBean> getChildren() {
        return children;
    }

    public void setChildren(HashMap<String, ViewBean> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "ViewBean{" +
                "url='" + url + '\'' +
                ", viewType=" + viewType +
                ", className='" + className + '\'' +
                ", containerId=" + containerId +
                ", description='" + description + '\'' +
                ", children=" + children +
                '}';
    }
}
