package com.llz.childrennoisedetect.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.llz.childrennoisedetect.R;

import java.io.File;
import java.text.DecimalFormat;


/**
 * Created by yaoshibang on 2014/12/15.
 */
public class AppConfig {

    /**
     * Bugly 开关 
     */
//    public final static boolean isBugly = true;

    public final static String AppName = MyApplication.getInstance().getString(R.string.app_name);
    public final static String appMainPath = Environment.getExternalStorageDirectory().getPath() +
            File.separator + AppName + File.separator;
    public static final String logPath = appMainPath + "log" + File.separator;
    public static final String imgPath = appMainPath + "img" + File.separator;

    /**
     * this is userInfo flag
     */
    private final static String flag_deviceID = "deviceID";		//设备ID

    /** DecimalFormat for price value **/
    // 建议统一使用DecimalUtil.FormatMoney(...)
    @Deprecated
    public final static DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    @Deprecated
    public final static DecimalFormat decimalFormat1 = new DecimalFormat("#0");

    private static Context context;
    private static SharedPreferences userInfo;

    /**设置音量阈值 和 持续时间**/
    public static final String flag_volume_threshold = "volume_threshold";
    public static final String flag_volume_continue_time = "volume_continue_time";
    public static final String flag_phone = "phone";

    public static void init() {

        context = MyApplication.getInstance();
        userInfo = context.getSharedPreferences("app_config", 0);


        initScreenSize();
        initDefault();

    }

    private static void initDefault() {
        AppConfig.setUserDefault(flag_volume_threshold, 55);   //默认音量阈值 55db
        AppConfig.setUserDefault(flag_volume_continue_time, 2);   //默认噪音持续时间 2s
        AppConfig.setUserDefault(flag_phone, "18933948952");   //默认通知手机
    }
    public static boolean isExitsSdCard() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    public static int getScreenWidth() {

        int screen_width    = userInfo.getInt("screen_width", -1);

        return screen_width;
    }

    public static int getScreenHeight() {

        int screen_height   = userInfo.getInt("screen_height", -1);

        return screen_height;
    }


    private static void initScreenSize() {

        int screen_width    = userInfo.getInt("screen_width", -1);
        int screen_height   = userInfo.getInt("screen_height", -1);

        if (screen_width == -1 || screen_height == -1) {

            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            screen_width = dm.widthPixels;
            screen_height = dm.heightPixels;

            userInfo.edit().putInt("screen_width", screen_width).commit();
            userInfo.edit().putInt("screen_height", screen_height).commit();
        }

    }



    public static <T>T getUserDefault(String flag, Class<T> cls) {

        try {

            Object result = null;

            if (cls.equals(boolean.class)) {
                result = userInfo.getBoolean(flag, false);
            } else
            if (cls.equals(String.class)) {
                result = userInfo.getString(flag, null);
            } else
            if (cls.equals(int.class)) {
                result = userInfo.getInt(flag, -1);
            } else
            if (cls.equals(long.class)) {
                result = userInfo.getLong(flag, 0);
            }
            return (T) result;

        } catch (Exception ex) {
            Log.e("Appconfig", ex.getStackTrace().toString());
        }

        return null;
    }

    public static void setUserDefault(String flag, Object obj) {

        if (obj instanceof Boolean) {
            userInfo.edit().putBoolean(flag, (Boolean) obj).commit();

        } else
        if (obj instanceof String) {
            userInfo.edit().putString(flag, (String) obj).commit();
        } else
        if (obj instanceof Integer) {
            userInfo.edit().putInt(flag, (Integer) obj).commit();
        } else
        if (obj instanceof Long) {
            userInfo.edit().putLong(flag, (Long) obj).commit();
        }
    }


    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }




}
