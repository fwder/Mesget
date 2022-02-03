package com.weclont.mesget;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application{

    /**
     * 全局的上下文
     */
    private static Context ServiceContext;
    private static String MCName;
    public static String Longitude; //经度
    public static String Latitude; //纬度
    public static String Accuracy; //精度
    public static String LocationTime; //相对定位时间
    public static String LocationType; //返回结果类型
    public static boolean result = false;
    public static int sume = 0;
    public static boolean isLocationOK = false; //判断当前是否定位成功

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static Context getServiceContext(){
        return ServiceContext;
    }
    public static String getMCName(){
        return MCName;
    }
    public static void setServiceContext(Context s){
        ServiceContext = s;
    }
    public static void setMCName(String s){
        MCName = s;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

}
