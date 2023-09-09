package com.life.app;


import android.app.Application;
import android.util.Log;



import com.life.annotation.EnableRouter;

@EnableRouter
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        Context applicationContext = getApplicationContext();
//        registerActivityLifecycleCallbacks(com.life.listener.RouterActivityLifecycleListener.getInstance(this.getApplicationContext()));
//        registerActivityLifecycleCallbacks(RouterActivityLifecycleListener.getInstance(getApplicationContext()));
        Log.d("MyApplication", "test");
    }
    /*  public static Context mContext;
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
    }
*/

}
