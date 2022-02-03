package com.weclont.mesget;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;

public class TooltipActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tooltip);

        Intent intent = getIntent();
        String text = intent.getStringExtra("Text");

        if (text.equals("")) {
            finish();
        }

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
