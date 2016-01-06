package com.llz.childrennoisedetect.config;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

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

        //bugly init
        CrashReport.initCrashReport(getApplicationContext(), "900016375", false);
    }

    public static MyApplication getInstance(){
      return application;
    }
}
