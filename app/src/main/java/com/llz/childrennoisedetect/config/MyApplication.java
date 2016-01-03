package com.llz.childrennoisedetect.config;

import android.app.Application;

/**
 * Created by ysbang on 2015/12/30.
 */
public class MyApplication extends Application {
    private static MyApplication application;
    @Override
    public void onCreate() {
        super.onCreate();

        application=this;
        AppConfig.init();
    }

    public static MyApplication getInstance(){
      return application;
    }
}
