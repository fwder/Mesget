package com.weclont.mesget;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainService extends Service {


    public static final int NOTICE_ID = 145;
    private static final String TAG = "MainService";
    public GPSLocated located = new GPSLocated("");
    public Timer timer_update = new Timer();
    public TimerTask timer_task_update;

    public void onCreate() {
        super.onCreate();

        //初始化
        SharedPreferences sp = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        MainApplication.setMCName(sp.getString("MCName", ""));
        MainApplication.setServiceContext(this);
        MainApplication.setServerIP(sp.getString("IP", ""));
        MainApplication.setServerPort(sp.getString("Port", ""));

        // 开启自动更新
        checkApplicationUpdate();

        //开启定位sdk
        try {
            located.openlocated(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //持续监听服务器是否有请求
        new CmdThingToDo().start();

        Log.e(TAG, "服务开启完毕");
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

    // 定时检查更新任务
    public void checkApplicationUpdate() {
        if (timer_update == null) {
            timer_update = new Timer();
        }

        if (timer_task_update == null) {
            timer_task_update = new TimerTask() {
                @Override
                public void run() {

                    Log.e(TAG, "run: 自动更新开始运行");

                    String url = "https://api.fwder.cn/mesget/version";
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).build();
                    Log.e(TAG, "run: 读取更新文件");
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String result = response.body().string();
                                Log.e(TAG, "onResponse: 读取到结果：" + result);

                                //读取在线版本
                                int versionCode = Integer.parseInt(stringMatch("versionCode", result));
                                String versionName = stringMatch("versionName", result);
                                String URL = stringMatch("URL", result);

                                //读取当前版本
                                PackageManager pm = MainService.this.getPackageManager();//context为当前Activity上下文
                                PackageInfo pi = null;

                                try {
                                    pi = pm.getPackageInfo(MainService.this.getPackageName(), 0);
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "onResponse: 版本读取失败！");
                                }

                                //输出路径
                                String FileDir_all = getFilesDir().getAbsolutePath() + "/mesget.apk";
                                String FileDir_dir = getFilesDir().getAbsolutePath();
                                String FileDir_file = "/mesget.apk";
                                Log.e(TAG, "onResponse: 下载保存位置：" + FileDir_all);

                                if (versionCode > pi.versionCode) {

                                    Log.e(TAG, "onResponse: 检测到有新版本，新版本Code：" + versionCode + "，新版本代号：" + versionName + "，马上更新");

                                    // 当之前下载的apk存在时，直接删除
                                    fileIsExistsAndDelete(FileDir_all);

                                    // 自动下载apk
                                    Log.e(TAG, "onResponse: 开始自动下载apk");
                                    // url服务器地址，saveurl是下载路径，fileName表示的是文件名字
                                    DownloadUtil.get().download(MainService.this, URL, FileDir_dir, FileDir_file, new DownloadUtil.OnDownloadListener() {
                                        @Override
                                        public void onDownloadSuccess() {

                                            Log.e(TAG, "onDownloadSuccess: 下载成功！");

                                            //读取当前版本
                                            PackageManager pm = MainService.this.getPackageManager();
                                            PackageInfo pi = null;

                                            try {
                                                pi = pm.getPackageInfo(MainService.this.getPackageName(), 0);
                                            } catch (PackageManager.NameNotFoundException e) {
                                                e.printStackTrace();
                                                Log.e(TAG, "onResponse: 版本读取失败！");
                                            }

                                            Log.e(TAG, "onResponse: 安装路径为：" + FileDir_all);

                                            //下载完毕，进入安装
                                            String text = "setNewVersion";
                                            Intent intent = new Intent(MainApplication.getServiceContext(), TooltipActivity.class);
                                            intent.putExtra("Text", text);
                                            intent.putExtra("NewVersionName", versionName);
                                            intent.putExtra("NowversionName", pi.versionName);
                                            intent.putExtra("FileDir", FileDir_all);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            MainApplication.getServiceContext().startActivity(intent);

                                        }

                                        @Override
                                        public void onDownloading(int progress) {

                                            Log.e(TAG, "onDownloading: 下载中，进度：" + progress + "%");

                                        }

                                        @Override
                                        public void onDownloadFailed() {

                                            Log.e(TAG, "onDownloadFailed: 下载失败");

                                        }
                                    });
                                } else {
                                    Log.e(TAG, "onResponse: 当前没有更新");
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }
                    });

                }
            };
        }

        try {
            timer_update.schedule(timer_task_update, 0, 60000); // 每隔一分钟检查一次
        } catch (Exception e) {
            Log.e(TAG, "checkApplicationUpdate: 已经启动定时检查更新服务！");
        }

    }

    //判断文件是否存在
    public void fileIsExistsAndDelete(String strFile) {
        File f = new File(strFile);
        if (f.exists()) {
            f.delete();
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

}
