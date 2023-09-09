package com.life.manager;

import com.life.bean.RedirectQueueTask;
import com.life.processor_utils.ObjectUtils;
import com.life.processor_utils.RouterLog;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class RedirectThreadManager {

    private Map<String, Thread> threadMap;
    //跳转任务集合
    private ArrayList<RedirectQueueTask> redirectQueueTaskArrayList;

    private static class RedirectThreadManagerHolder {
        private static final RedirectThreadManager redirectManager = new RedirectThreadManager();
    }

    public static RedirectThreadManager getInstance() {
        return RedirectThreadManagerHolder.redirectManager;
    }

    public RedirectThreadManager() {
        threadMap = new HashMap<>();
        redirectQueueTaskArrayList = new ArrayList<>();
    }


    public synchronized void addTask(String taskKey, Runnable task) {
        Thread oldThread = threadMap.get(taskKey);
        if (ObjectUtils.isNotEmpty(oldThread) && oldThread.isAlive()) {
            // 同名任务正在执行，中断它
            oldThread.interrupt();
        }
        Thread thread = new Thread(() -> {
            try {
                RouterLog.i("The action is in progress for navigation.");
                task.run();
            } finally {
                threadMap.remove(taskKey);
//                Thread.currentThread().interrupt();
            }
        });
        threadMap.put(taskKey, thread); // 把该线程装入HashMap中,不执行
    }

    /**
     * 正式执行跳转任务
     *
     * @param taskKey fullClassName
     */
    public void execTask(String taskKey) {
        Thread thread = threadMap.get(taskKey);
        if (ObjectUtils.isNotEmpty(thread)) {
            thread.start();
        }
    }

    /**
     * 注册任务队列
     *
     * @param taskQueue RedirectQueueTask
     */
    public void registerTask(RedirectQueueTask taskQueue) {
        redirectQueueTaskArrayList.add(taskQueue);
    }

    /**
     * 因为Activity和Fragment跳转都是存在时序问题,也就是说, 执行跳转任务的时候需要跳转的目标类做好视图初始化
     * 因此添加一个触发方法, 当目标视图类(Fragment)的视图初始化好后再进行跳转
     *
     * @param viewName FragmentClassName
     */
    public synchronized void trigger(String viewName) {
        RouterLog.i("RedirectThreadManager trigger...viewName: " + viewName);
        Iterator<RedirectQueueTask> iterator = redirectQueueTaskArrayList.iterator();
        while (iterator.hasNext()) {
            RedirectQueueTask redirectQueueTask = iterator.next();
            if (redirectQueueTask.getTaskQueue().isEmpty()) {
                redirectQueueTaskArrayList.remove(redirectQueueTask);
            } else {
                redirectQueueTask.trigger(viewName);
            }
        }
    }

}



