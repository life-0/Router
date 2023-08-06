package com.life.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.life.annotation.EnableRouter;
import com.life.router.Router;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@EnableRouter
public class MyApplication extends Application {
    public static Context mContext;
    private Activity currentActivity;
    private final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //反射初始化
        Router router = Router.getInstance();
        try {
            Method initMethod = Router.class.getMethod("init", Context.class);
            initMethod.invoke(router, getApplicationContext());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // 注册 ActivityLifecycleCallbacks 监听 Activity 生命周期
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // 在 Activity 创建时保存当前 Activity 实例
                currentActivity = activity;
                Log.d(TAG, activity.getLocalClassName());
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });

    }

    // 获取当前运行的 Activity 的实例对象
    public Activity getCurrentActivity() {
        return currentActivity;
    }

}
