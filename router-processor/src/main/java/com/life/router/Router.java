

package com.life.router;

import android.content.Context;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class Router {

    private static class RouterHolder {
        private final static Router router = new Router();
    }

    public Router() {

    }

    public void init(Context context) {

    }

    private Context getContext() {
        return null;

    }

    public static Router getInstance() {
        return RouterHolder.router;
    }


    /**
     * 获取当前应用的包名
     *
     * @return String packageName
     */

    private String getCurrentPackageName() {
        return null;
    }


    /**
     * 获取映射表路径对应的类名
     *
     * @param url 路径
     * @return Activity全类名
     */

    private String getRouterMapping(String url) {
        return null;
    }

    public void redirect(String url, Bundle bundle) {
    }

    public void redirect(String url) {
    }
}

