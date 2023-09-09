package com.life.bean;

import com.life.manager.RedirectThreadManager;
import com.life.processor_utils.ObjectUtils;
import com.life.processor_utils.RouterLog;
import com.life.processor_utils.UrlUtil;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/*
 *@Author: life-0
 *@ClassName: RedirectQueueTask
 *@Date: 2023/9/8 10:42
 *TODO @Description:    链接跳转任务

 */
public class RedirectQueueTask {
    private Queue<AbstractMap.SimpleEntry<String, Runnable>> taskQueue;
    private String parentClassName;

    /**
     * 注册任务队列
     *
     * @param parentClassName 获取根Activity类名当作元素特别的表头
     * @param taskQueue       Queue<AbstractMap.SimpleEntry<String, Runnable>>
     */
    public RedirectQueueTask(String parentClassName, Queue<AbstractMap.SimpleEntry<String, Runnable>> taskQueue) {
        this.taskQueue = taskQueue;
        this.parentClassName = parentClassName;
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }

    public Queue<AbstractMap.SimpleEntry<String, Runnable>> getTaskQueue() {
        return taskQueue;
    }

    /**
     * 因为Activity和Fragment跳转都是存在时序问题,也就是说, 执行跳转任务的时候需要跳转的目标类做好视图初始化
     * 因此添加一个触发方法, 当目标视图类(Fragment)的视图初始化好后再进行跳转
     *
     * @param viewName FragmentClassName or ActivityClassName
     */
    public void trigger(String viewName) {
        RouterLog.i("RedirectQueueTask trigger...viewName: " + viewName);
        if (!taskQueue.isEmpty()) {
            if (parentClassName.equals(viewName)) {
                //头部元素出队列执行
                AbstractMap.SimpleEntry<String, Runnable> runnableRedirectEntry = taskQueue.poll();
                new Thread(runnableRedirectEntry.getValue()).start();//执行跳转任务
                parentClassName = runnableRedirectEntry.getKey();   //设置当前父元素
            }
        }
    }
}
