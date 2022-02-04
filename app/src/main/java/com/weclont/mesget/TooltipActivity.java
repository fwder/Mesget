package com.weclont.mesget;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;

import java.io.File;


public class TooltipActivity extends AppCompatActivity {

    private static final String TAG = "TooltipActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tooltip);

        Intent intent = getIntent();
        String text = intent.getStringExtra("Text");

        if (text.equals("")) {
            finish();
        }

        // 定位服务开启提示框
        if (text.equals("setEnabledLocationService")) {
            // 弹出对话框
            new AlertDialog.Builder(this)
                    .setTitle("提示")//设置标题
                    .setMessage("检测到您关闭了设备的定位服务，您需要重新开启定位服务才能使用该设备！")//提示消息
                    .setIcon(R.drawable.sysu)//设置图标
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        //点击确定按钮执行的事件
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                            finish();
                            return true;
                        }
                    })
                    .create()//创建对话框
                    .show();//显示对话框
            return;
        }

        //更新提示框
        if (text.equals("setNewVersion")) {

            //获取信息
            String NowversionName = intent.getStringExtra("NowversionName");
            String NewVersionName = intent.getStringExtra("NewVersionName");
            String FileDir = intent.getStringExtra("FileDir");

            // 弹出对话框
            new AlertDialog.Builder(this)
                    .setTitle("发现新版本")//设置标题
                    .setMessage("检测到新版本：" + NewVersionName + "\n当前版本：" + NowversionName + "\n安装包已下载完成，请立即更新，并在安装完成之后点击启动，否则您的设备将无法使用！")//提示消息
                    .setIcon(R.drawable.sysu)//设置图标
                    .setCancelable(false)
                    .setPositiveButton("安装", new DialogInterface.OnClickListener() {

                        //点击确定按钮执行的事件
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // 开始安装应用通用方法
                            File apk = new File(FileDir);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                Uri uri = FileProvider.getUriForFile(TooltipActivity.this, "com.weclont.mesget.fileprovider", apk);
                                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                            }else{
                                intent.setDataAndType(Uri.fromFile(apk),"application/vnd.android.package-archive");
                            }
                            try {
                                startActivity(intent);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            finish();

                        }
                    })
                    .create()//创建对话框
                    .show();//显示对话框
            return;
        }

        // 弹出对话框
        new AlertDialog.Builder(this)
                .setTitle("您收到一条新消息")//设置标题
                .setMessage(text)//提示消息
                .setIcon(R.drawable.sysu)//设置图标
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    //点击确定按钮执行的事件
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        finish();
                        return true;
                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onDestroy() {

        // 重启Service
        Intent intent = new Intent(this, MainService.class);
        startService(intent);

        // 重启Service之后再销毁
        super.onDestroy();
    }

}
