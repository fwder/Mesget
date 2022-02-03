package com.weclont.mesget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class StartBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//            Intent intentactivity = new Intent(context, MainActivity.class);  // 要启动的Activity
            Intent intentservice = new Intent(context,MainService.class);
            //这句话必须加上才能开机自动运行app的界面
//            intentactivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentservice.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //2.如果自启动Activity
//            context.startActivity(intentactivity);
            //3.如果自启动服务
            context.startService(intentservice);
        }
    }
}
