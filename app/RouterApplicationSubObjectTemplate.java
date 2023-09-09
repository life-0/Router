package com.life.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.life.common.ISubscriber;
import com.life.manager.RedirectThreadManager;
import com.life.router.Router;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RouterApplicationSubObjectTemplate extends MyApplication {
    private static Context mContext;
    private Activity currentActivity;
    private final String TAG = this.getClass().getSimpleName();
    private List<ISubscriber> subscribers = new ArrayList<>();

    public RouterApplicationSubObjectTemplate() {
        mContext = getApplicationContext();
        Router router = Router.getInstance();
        try {
            Method initMethod = router.getClass().getDeclaredMethod("init", Context.class);
            initMethod.setAccessible(true);
            initMethod.invoke(router, mContext);
            initMethod.setAccessible(false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        // 注册 ActivityLifecycleCallbacks 监听 Activity 生命周期
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // 在 Activity 创建时保存当前 Activity 实例
                currentActivity = activity;
                RedirectThreadManager.getInstance().execTask(currentActivity.getPackageName() + '.' + currentActivity.getLocalClassName());
                Log.d(TAG, "Router Application execTask.." + activity.getLocalClassName());
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
