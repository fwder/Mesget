package com.weclont.mesget;


import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //开始运行服务
        SharedPreferences sp = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        EditText ett_mcname = findViewById(R.id.ett_mcname);
        EditText ett_ip = findViewById(R.id.ett_ip);
        EditText ett_port = findViewById(R.id.ett_port);

        //检查配置
        if (!sp.getString("MCName", "").equals("")) {
            ett_mcname.setText(sp.getString("MCName", ""));
            ett_ip.setText(sp.getString("IP", ""));
            ett_port.setText(sp.getString("Port", ""));

            //检查是否已经申请权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                Toast.makeText(this, "用户授予的权限缺失！请先在系统设置中手动授予本应用所有申请的权限（定位，拍照权限需要选择“始终允许”）", Toast.LENGTH_LONG).show();
                startApplicationSetting(MainActivity.this);
            }

            new AlertDialog.Builder(this)
                    .setTitle("提示")//设置标题
                    .setMessage("配置成功读取。\n设备名称：" + sp.getString("MCName", "") + "\n点击运行可后台执行监控服务，点击修改可更改当前配置。")//提示消息
                    .setIcon(R.drawable.sysu)//设置图标
                    .setPositiveButton("运行", new DialogInterface.OnClickListener() {
                        //点击确定按钮执行的事件
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("提示")//设置标题
                                    .setMessage("点击确定开始运行后，您需要检查任务栏是否有Mesget的通知，以此来确认服务是否运行成功。（弹出通知可能有延时）")//提示消息
                                    .setIcon(R.drawable.sysu)//设置图标
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        //点击确定按钮执行的事件
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.e("TAG", "onCreate: 开始运行服务...");
                                            startService(new Intent(MainActivity.this, MainService.class));
                                            MainActivity.this.finish();
                                        }
                                    })
                                    .create()//创建对话框
                                    .show();//显示对话框
                        }
                    })
                    .setNegativeButton("修改", new DialogInterface.OnClickListener() {
                        //点击确定按钮执行的事件
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create()//创建对话框
                    .show();//显示对话框
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("提示")//设置标题
                    .setMessage("首次运行，请先在系统设置中手动授予本应用所有申请的权限（定位，拍照权限需要选择“始终允许”），之后在应用内设置本设备名称和定位间隔时间，保存后即可运行程序！\n即将尝试跳转权限设置页面...")//提示消息
                    .setIcon(R.drawable.sysu)//设置图标
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        //点击确定按钮执行的事件
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //检查是否已经申请权限
                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                                startApplicationSetting(MainActivity.this);
                            }
                        }
                    })
                    .create()//创建对话框
                    .show();//显示对话框
        }

    }

    public void btnonClickSaveMCNameData(View view) {
        SharedPreferences sp = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        EditText ett_mcname = findViewById(R.id.ett_mcname);
        EditText ett_ip = findViewById(R.id.ett_ip);
        EditText ett_port = findViewById(R.id.ett_port);
        if (ett_mcname.getText().toString().equals("")) {
            Toast.makeText(this, "设备名称没填写啊~", Toast.LENGTH_SHORT).show();
            return;
        }else if(ett_ip.getText().toString().equals("")){
            Toast.makeText(this, "服务器IP地址没填写啊~", Toast.LENGTH_SHORT).show();
            return;
        }else if(ett_port.getText().toString().equals("")){
            Toast.makeText(this, "服务器端口没填写啊~", Toast.LENGTH_SHORT).show();
            return;
        }
        sp.edit().putString("MCName", ett_mcname.getText().toString()).apply();
        sp.edit().putString("IP", ett_ip.getText().toString()).apply();
        sp.edit().putString("Port", ett_port.getText().toString()).apply();
        new AlertDialog.Builder(this)
                .setTitle("提示")//设置标题
                .setMessage("保存成功，重启以启动服务！")//提示消息
                .setIcon(R.drawable.sysu)//设置图标
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    //点击确定按钮执行的事件
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                        finish();
                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }

    @Override
    public void onDestroy() {

        // 检测是否首次运行程序
        SharedPreferences sp1 = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if(!sp1.getString("MCName", "").equals("")){
            // 重启Service
            Intent intent = new Intent(this, MainService.class);
            startService(intent);
        }

        // 操作完之后再销毁
        super.onDestroy();

    }

    public void onFinishMainActivity(View view) {
        finish();
    }

    /**
     * 打开设置应用权限页面
     *
     * @param context
     */
    public static void startApplicationSetting(Context context) {
        try {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                intent.putExtra(Notification.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            } else {
                //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                intent.putExtra("app_package", context.getPackageName());
                intent.putExtra("app_uid", context.getApplicationInfo().uid);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
            } catch (Exception ex) {
                e.printStackTrace();
            }
        }
    }
}
