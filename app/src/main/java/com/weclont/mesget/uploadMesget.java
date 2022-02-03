package com.weclont.mesget;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class uploadMesget {

    public String CommandResult;
    public String MCName;

    public uploadMesget(){
        MCName = MainApplication.getMCName();
    }



    public void logcatMesget(String main, String name, String func, String level, String msg) throws Exception {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat nowtimeForm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowtime = nowtimeForm.format(date);
        String logcat = "[" + nowtime + "] [MCName:" + name + ", Func:" + func + ", Level:" + level + "] " + msg;
        String url = "https://mesget.fwder.cn/?main=" + main + "&name=" + name + "&text="+urlEncoded(logcat);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("OkHttp", "访问错误！");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                Log.e("OkHttp", "请求成功！");
            }
        });
        while(call.isCanceled()){
            try {
                Thread.sleep(50);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public void receiveCommand(String name) throws InterruptedException {
        MCName = name;
        String url = "https://mesget.fwder.cn/?main=receive&name="+name;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("OkHttp", "访问错误！");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.e("OKHttp", "receiveCommand: 命令获取成功！");
                CommandResult = response.body().string();
            }
        });

        while (call.isCanceled()) {
            try {
                Thread.sleep(50);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }


    }

    public String urlEncoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            return "";
        }
        try {
            String str = new String(paramString.getBytes(), StandardCharsets.UTF_8);
            str = URLEncoder.encode(str, "UTF-8");
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
