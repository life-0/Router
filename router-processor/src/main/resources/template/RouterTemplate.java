package com.life.router;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.SimpleArrayMap;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.life.bean.BeanDefinition;
import com.life.bean.Constant;
import com.life.bean.Message;
import com.life.bean.RedirectQueueTask;
import com.life.bean.Subscriber;
import com.life.bean.ViewBean;
import com.life.bean.ViewType;
import com.life.exception.BeansException;
import com.life.factory.DefaultListableBeanFactory;
import com.life.manager.RedirectThreadManager;
import com.life.processor_utils.ObjectUtils;
import com.life.processor_utils.RouterLog;
import com.life.processor_utils.UrlUtil;

public class Router {

    private WeakReference<Context> mContextReference;

    private static HashMap<String, ViewBean> routerMapping;

    private final static String TAG = Router.class.getSimpleName();

    //    Fragment工厂类
    private DefaultListableBeanFactory viewFactory;

    //container子布局id
    private static int fragment_layout_id;

    //获取当前正在运行的Activity类的弱引用, 防止Activity无法正常销毁
    private WeakReference<AppCompatActivity> currentActivityWeakReference;

    //创建订阅者子类,并注册
    private class RouterSubscriber extends Subscriber {

        public RouterSubscriber(String channelId) {
            super(channelId);
        }

        @Override
        public void receiveMessage(Message message) {
            super.receiveMessage(message);
            Object data = message.getContent();
            if (data instanceof AppCompatActivity) {
                currentActivityWeakReference = new WeakReference<>((AppCompatActivity) data);
            }
        }
    }

    private static class RouterHolder {

        private final static Router router = new Router();
    }

    public Router() {
        //给指定频道注册订阅者
        RouterSubscriber subscriber = new RouterSubscriber(Constant.ActivityChangeChannel);
    }

    public static Router getInstance() {
        return RouterHolder.router;
    }

    /**
     * 初始化
     *
     * @param context ApplicationContext
     */
    public void init(Context context) {
        mContextReference = new WeakReference<>(context);
        viewFactory = new DefaultListableBeanFactory();
        //获取生成的路由总表类, 并且实例化 mappingClassName
        try {
            Class<?> aClass = Class.forName(mappingClassName);
            Object instance = aClass.newInstance();
            Method getMethod = aClass.getMethod("get");
            routerMapping = (HashMap<String, ViewBean>) getMethod.invoke(instance);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        //初始化视图工厂类
        initFactory(viewFactory, routerMapping);
    }

    /**
     * 初始化工厂类, 用于创建view(Fragment/Activity)的实例
     *
     * @param defaultListableBeanFactory
     * @param routerMapp
     */
    private void initFactory(DefaultListableBeanFactory defaultListableBeanFactory, HashMap<String, ViewBean> routerMapp) {
        if (ObjectUtils.isNotEmpty(routerMapp)) {
            //添加fragment的注册信息
            routerMapp.entrySet().forEach(viewBeanEntry -> {
                ViewBean viewBeanEntryValue = viewBeanEntry.getValue();
                String className = viewBeanEntryValue.getClassName();
                try {
                    Class<?> fragmentClass = Class.forName(className);
                    BeanDefinition beanDefinition = new BeanDefinition(fragmentClass);
                    //注册到工厂类中
                    defaultListableBeanFactory.registerBeanDefinition(className, beanDefinition);
                    RouterLog.i(className + " register");
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                initFactory(defaultListableBeanFactory, viewBeanEntryValue.getChildren());
            });
        }
    }

    /**
     * 获取ApplicationContext
     *
     * @return
     */
    private Context getContext() {
        if (mContextReference != null) {
            return mContextReference.get().getApplicationContext();
        } else {
            return null;
        }
    }

    /**
     * 获取当前的activity
     *
     * @return AppCompatActivity
     */
    private AppCompatActivity getCurrentActivity() {
        if (ObjectUtils.isNotEmpty(currentActivityWeakReference)) {
            return currentActivityWeakReference.get();
        } else {
            return null;
        }
    }

    /**
     * 获取当前应用的包名
     *
     * @return String packageName
     */
    private String getCurrentPackageName() {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getContext().getPackageName(), 0);
            return packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 带参跳转
     *
     * @param url
     * @param bundle params
     */
    public void redirect(String url, Bundle bundle) {
        //分割页面
        ArrayList<String> urlList = UrlUtil.getInstance().splitUrl(url);
        //只获取第一个Activity,用于创建第一个任务
        ViewBean firstViewBean = routerMapping.get(urlList.remove(0));
        //主页面对象
        AppCompatActivity activity = null;
        //获取目标Activity的类名
        String taskKey = firstViewBean.getClassName();
        //优先判断当前的Activity不是空的,否则不执行跳转
        AppCompatActivity currentActivity = getCurrentActivity();
        if (ObjectUtils.isEmpty(currentActivity)) {
            RouterLog.e("Current Activity is null");
            return;
        }
        //activityt跳转用的intent
        Intent intent = new Intent();
        // 判断是否跳转到自身的Activity界面上
        if (taskKey.contains(currentActivity.getLocalClassName())) {
            if (urlList.size() != 0) {
                //如果有子页面,那就装填线程任务
                Queue<AbstractMap.SimpleEntry<String, Runnable>> redirectTask = createRedirectTask(urlList, bundle, firstViewBean);
                //实际的跳转任务
                RedirectQueueTask redirectQueueTask = new RedirectQueueTask(taskKey, redirectTask);
                //获取跳转队列的头部元素
                AbstractMap.SimpleEntry<String, Runnable> peekTask = redirectTask.poll();
                //重新设置头部元素
                redirectQueueTask.setParentClassName(peekTask.getKey());
                // 装填任务
                RedirectThreadManager.getInstance().registerTask(redirectQueueTask);
                peekTask.getValue().run();
                //                RedirectThreadManager.getInstance().trigger(peekTask.getKey());
            } else {
                //只跳转Activity的话就装填参数
                RouterLog.e(" The navigated page must rely on an Activity!");
            }
        } else {
            // 获取切换目标Activity页面的类名
            String targetViewName = firstViewBean.getClassName();
            //  赋予fragment_layout的资源id
            fragment_layout_id = firstViewBean.getContainerId() != -1 ? firstViewBean.getContainerId() : fragment_layout_id;
            ComponentName componentName = new ComponentName(getCurrentPackageName(), targetViewName);
            //提示跳转
            RouterLog.redirectTip(targetViewName);
            intent.setComponent(componentName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (urlList.size() != 0) {
                //如果只跳转Activity,没有子页面,那就不装填线程任务
                //创建一个跳转任务队列
                Queue<AbstractMap.SimpleEntry<String, Runnable>> redirectTask = createRedirectTask(urlList, bundle, firstViewBean);
                //实际的跳转任务
                RedirectQueueTask redirectQueueTask = new RedirectQueueTask(taskKey, redirectTask);
                // 装填任务
                RedirectThreadManager.getInstance().registerTask(redirectQueueTask);
            } else {
                //只跳转Activity的话就装填参数
                intent.putExtras(bundle);
            }
            // 执行跳转页面
            getContext().startActivity(intent);
        }
    }

    /**
     * 创建子页面跳转任务
     *
     * @param urlList      fragmentURLList
     * @param bundle       params
     * @param rootViewBean RootActivityViewBean
     * @return
     */
    private Queue<AbstractMap.SimpleEntry<String, Runnable>> createRedirectTask(ArrayList<String> urlList, Bundle bundle, ViewBean rootViewBean) {
        //fragment
        HashMap<String, ViewBean> tempChildrenMap = rootViewBean.getChildren();
        //  赋予fragment_layout的资源id
        fragment_layout_id = rootViewBean.getContainerId() != -1 ? rootViewBean.getContainerId() : fragment_layout_id;
        //存放跳转任务
        Queue<AbstractMap.SimpleEntry<String, Runnable>> taskQueue = new LinkedList<>();
        ViewBean tempViewBean = null;
        for (int i = 0; i < urlList.size(); i++) {
            final String url = urlList.get(i);
            final ViewBean currentViewBean = tempChildrenMap.get(url);
            final ViewBean parentViewBean;
            if (i == 0) {
                parentViewBean = rootViewBean;
            } else {
                parentViewBean = tempViewBean;
            }
            Runnable task = new Runnable() {

                //设置父页面
                //上一级子页面对象
                @Override
                public void run() {
                    //将要拉起的Fragment
                    Fragment nextFragment;
                    // 使用工厂类获取fragment实例
                    try {
                        nextFragment = (Fragment) viewFactory.getBean(currentViewBean.getClassName());
                    } catch (BeansException e) {
                        throw new RuntimeException(e);
                    }
                    //判断是否是从Activity根页面启动
                    if (parentViewBean.getViewType() == ViewType.ACTIVITY) {
                        RouterLog.i("parentViewBean is ACTIVITY");
                        //获取当前的Activity界面
                        AppCompatActivity taskCurrentActivity = getCurrentActivity();
                        FragmentManager supportFragmentManager = taskCurrentActivity.getSupportFragmentManager();
                        //如果存在 Fragment在当前的容器,就直接清空
                        Fragment currentFragment = supportFragmentManager.findFragmentById(fragment_layout_id);
                        if (currentFragment != null) {
                            supportFragmentManager.beginTransaction().remove(currentFragment).commitNow();
                        }
                        //重新拉起fragment页面
                        taskCurrentActivity.getSupportFragmentManager().beginTransaction().replace(fragment_layout_id, nextFragment).commit();
                    } else if (parentViewBean.getViewType() == ViewType.FRAGMENT) {
                        RouterLog.i("parentViewBean is FRAGMENT");
                        // 执行跳转
                        // 添加到返回栈，实现返回键返回父 Fragment
                        Fragment parentFragment = null;
                        try {
                            //获取父Fragment
                            parentFragment = (Fragment) viewFactory.getBean(parentViewBean.getClassName());
                            fragment_layout_id = parentViewBean.getContainerId() != -1 ? parentViewBean.getContainerId() : fragment_layout_id;
                            FragmentManager childFragmentManager = parentFragment.getChildFragmentManager();
                            //如果存在 Fragment在当前的容器,就直接清空
                            Fragment currentFragment = childFragmentManager.findFragmentById(fragment_layout_id);
                            if (currentFragment != null) {
                                childFragmentManager.beginTransaction().remove(currentFragment).commitNow();
                            }
                            //再重新执行替换
                            childFragmentManager.beginTransaction().replace(fragment_layout_id, nextFragment).commit();
                        } catch (BeansException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (parentViewBean.getViewType() == ViewType.DEFAULT) {
                        RouterLog.e("It was found that this class(" + currentViewBean.getClassName() + ") is not a Fragment or Activity.");
                    }
                }
            };
            //装填任务
            taskQueue.add(new AbstractMap.SimpleEntry<>(currentViewBean.getClassName(), task));
            tempChildrenMap = currentViewBean.getChildren();
            tempViewBean = currentViewBean;
        }
        return taskQueue;
    }
}
