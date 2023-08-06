

package com.life.router;

import android.content.Context;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.HashMap;


public class Router {
    private WeakReference<Context> mContextReference;
    private static HashMap<String, String> routerMapping;

    private static class RouterHolder {
        private final static Router router = new Router();
    }

    public Router() {

    }

    private void init(Context context) {
    }

    private Context getContext() {
        return mContextReference.get();

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
        return routerMapping.get(url);
    }

    public void redirect(String url, Bundle bundle) {

    }
}

