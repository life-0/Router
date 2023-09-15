package com.life.app;


import android.app.Application;
import android.util.Log;



import com.life.router.annotation.EnableRouter;

@EnableRouter
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyApplication", "test");
    }


}
