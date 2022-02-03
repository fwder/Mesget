package com.weclont.mesget;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class MainService extends Service {


    public static final int NOTICE_ID = 145;
    private static final String TAG = "MainService";
    public GPSLocated located = new GPSLocated("");
    public Context context = this;


    public void onCreate() {
        super.onCreate();

        //初始化
        SharedPreferences sp = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        MainApplication.setMCName(sp.getString("MCName", ""));
        MainApplication.setServiceContext(context);

        //开启定位sdk
        try {
            located.openlocated(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //持续监听服务器是否有请求
        new CmdThingToDo().start();

        Log.e(TAG, "Child服务和定位服务开启完毕！！");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //发通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //创建通知渠道实例
            //并为它设置属性
            //通知渠道的ID,随便写
            String id = "Mesget_Service_Notification_Channel";
            //用户可以看到的通知渠道的名字，R.string.app_name就是strings.xml文件的参数，自定义一个就好了
            CharSequence name = "Mesget - ━━━∑(ﾟ□ﾟ*川━";
            //用户可看到的通知描述
            String description = "Mesget -> Server √  (｀・ω・´)";
            //构建NotificationChannel实例
            NotificationChannel notificationChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
            //配置通知渠道的属性
            notificationChannel.setDescription(description);
            //在notificationManager中创建通知渠道
            manager.createNotificationChannel(notificationChannel);

            //蓝色字是个新方法，旧的被api放弃了
            Notification notification = new NotificationCompat.Builder(this, id)
                    .setContentTitle("Mesget - ━━━∑(ﾟ□ﾟ*川━")
                    .setContentText("Mesget -> Server √  (｀・ω・´)")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.child512)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_name))
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .build();
            startForeground(1, notification);
        }
        Log.e(TAG, "onStartCommand: 通知已经弹出，服务开始运行...");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 如果Service被杀死，干掉通知
        NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mManager.cancel(NOTICE_ID);
        Log.e(TAG, "DaemonService---->onDestroy，前台service被杀死");

        // 重启自己
        Intent intent = new Intent(getApplicationContext(), MainService.class);
        startService(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
