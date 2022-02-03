package com.weclont.mesget;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by weclont on 2022/1/29.
 */
public class WeclontNetWorkUtil {

    // 获取当前的网络状态是否可用
    public String getNetworkState(Context context) {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 获取NetworkInfo对象
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        // 遍历每一个对象
        for (NetworkInfo networkInfo : networkInfos) {
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED){
                // 网络状态可用
                return networkInfo.getTypeName();
            }
        }
        // 没有可用的网络
        return "NoNetWork";
    }
}
