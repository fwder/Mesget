package com.weclont.mesget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.PowerManager;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GPSLocated extends AMapLocation implements AMapLocationListener {

    private static final String TAG = "GPSLocated";
    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    public String times = "20000";

    public GPSLocated(String s) {
        super(s);
    }

    public void openlocated(Context context) throws Exception {
        //初始化
        Log.e(TAG, "onCreate: 开始运行定位服务...");

        //检查合规接口
        AMapLocationClient.updatePrivacyShow(context, true, true);
        AMapLocationClient.updatePrivacyAgree(context, true);
        //初始化AmapSDK
        mlocationClient = new AMapLocationClient(context);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(this);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(Integer.parseInt(times));
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();
        Log.e("GPSLocated", "onCreate: 定位服务运行完毕，持续定位模式。");

    }


    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

        //Location信息发送
        Log.e("GPSLocated", "onCreate: 获取到定位...");
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                Log.e(TAG, "onLocationChanged: 定位成功！");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                MainApplication.sume = 0;
                MainApplication.isLocationOK = true;
                MainApplication.Longitude = df.format(date);//定位时间
                MainApplication.Longitude = String.valueOf(amapLocation.getLongitude());//获取经度
                MainApplication.Latitude = String.valueOf(amapLocation.getLatitude());//获取纬度
                MainApplication.LocationType = String.valueOf(amapLocation.getLocationType());//获取当前定位结果来源，如网络定位结果，详见定位类型表
                MainApplication.Accuracy = String.valueOf(amapLocation.getAccuracy());//获取精度信息
                MainApplication.LocationTime = String.valueOf(amapLocation.getTime());//获取精度信息
            }else{
                Log.e(TAG, "onLocationChanged: 定位失败！");
                MainApplication.isLocationOK = false;
                MainApplication.sume++; //失败次数
                SharedPreferences sp = MainApplication.getServiceContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
                // 检查定位服务是否关闭，检测MCName是否配置
                if (!isLocServiceEnable(MainApplication.getServiceContext()) && !sp.getString("MCName", "").equals("")) {
                    // 定位服务被关闭，弹出提示框开启定位服务
                    String text = "setEnabledLocationService";
                    Intent intent = new Intent(MainApplication.getServiceContext(), TooltipActivity.class);
                    intent.putExtra("Text", text);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainApplication.getServiceContext().startActivity(intent);
                }
            }
        }


    }

    //判断定位服务是否打开
    public static boolean isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

}
