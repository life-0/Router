import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.life.bean.Constant;
import com.life.bean.Message;
import com.life.bean.Publisher;
import com.life.manager.RedirectThreadManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 监听activity回调,初始化Router
 */
public class RouterActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {
    private static volatile RouterActivityLifecycleListener instance;
    private final String TAG = this.getClass().getSimpleName();
    private Publisher activityChangerPublisher; //注册一个消息提供者

    public static RouterActivityLifecycleListener getInstance(Context applicationContext) {
        if (instance == null) {
            synchronized (RouterActivityLifecycleListener.class) {
                if (instance == null) {
                    instance = new RouterActivityLifecycleListener(applicationContext);
                }
            }
        }
        return instance;
    }

    public RouterActivityLifecycleListener(Context applicationContext) {
        //注册一个消息提供者 频道是ActivityChangeChannel
        activityChangerPublisher = new Publisher(Constant.ActivityChangeChannel);
        //初始化Router类
        com.life.router.Router router = com.life.router.Router.getInstance();
        try {
            Method initMethod = router.getClass().getDeclaredMethod("init", Context.class);
            initMethod.invoke(router, applicationContext);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
//        Log.i(TAG, "onActivityStarted.." + activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.i(TAG, "onActivityResumed.." + activity.getLocalClassName());
        //  在Activity 创建时保存当前 Activity 实例
        Activity currentActivity = activity;
        //  先通知所有这个频道的订阅者
        activityChangerPublisher.publishMessage(new Message(activity));
        //  再执行跳转任务
        RedirectThreadManager.getInstance().trigger(currentActivity.getPackageName() +
                '.' + currentActivity.getLocalClassName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
//        Log.i(TAG, "onActivityPaused.." + activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
//        Log.i(TAG, "onActivityStopped.." + activity.getLocalClassName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//        Log.i(TAG, "onActivitySaveInstanceState.." + activity.getLocalClassName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
//        Log.i(TAG, "onActivitySaveInstanceState.." + activity.getLocalClassName());
    }
}
