package com.weclont.mesget;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class socketMesget {

    private static final String TAG = "socketMesget";

    // Socket变量
    private Socket socket;
    /*连接线程*/
    private Thread connectThread;
    private Timer timer = new Timer();
    private TimerTask task;
    private OutputStream outputStream;
    private BufferedReader inputStream;
    private String response = "";

    private String ip = "8.210.199.163";
    private String port = "9011";


    /*默认重连*/
    private boolean isReConnect = true;

    public void onCreate() {

        if (socket == null) {
            try {
                socket = new Socket(ip, Integer.parseInt(port));
                socket.setSoTimeout(Integer.MAX_VALUE);

                //延时，检查连接
                if (socket != null && socket.isConnected()) {
                    //发送连接数据
                    outputStream = socket.getOutputStream();
                    outputStream.write(("<MCName:" + MainApplication.getMCName() + "><func:onCreateConnection><type:Child>\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                } else {
                    logerror("连接失败，尝试重连...");
                    resetConnection();
                    return;
                }
            } catch (IOException e) {
                Log.e(TAG, "run: 连接失败！");
                if (e instanceof SocketTimeoutException) {
                    logerror("连接超时，正在重连");
                    resetConnection();
                    return;

                } else if (e instanceof NoRouteToHostException) {
                    logerror("该地址不存在，请检查网络。");
                    resetConnection();
                    return;

                } else if (e instanceof ConnectException) {
                    logerror("连接异常或被拒绝，正在重连...");
                    resetConnection();
                    return;

                }

            }

            Log.e(TAG, "onConnectedServer: 连接成功！！！");

            //设置连接状态
            MainApplication.result = false;
            //发送定时数据
            sendBeatData();

        } else {
            Log.e(TAG, "onCreate: 不知道是什么毛病");
        }
    }

    /*定时发送数据*/
    public void sendBeatData() {
        if (timer == null) {
            timer = new Timer();
        }

        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        logerror("发送心跳数据...");
                        outputStream = socket.getOutputStream();
                        outputStream.write(("BeatData\n").getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    } catch (Exception e) {

                        /*发送失败说明socket断开了或者出现了其他错误*/
                        logerror("连接断开，正在重连");
                        /*重连*/
                        resetConnection();
                        e.printStackTrace();
                    }
                }
            };
        }

        try {
            timer.schedule(task, 0, 2000);
        } catch (Exception e) {
            Log.e(TAG, "sendBeatData:  已经创建了任务，不再创建");
        }

    }

    /*发送数据*/
    public void onSendMsg(String msg1) {
        if (socket != null && socket.isConnected()) {
            /*发送指令*/
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (socket.isConnected() && socket != null) {
                        try {
                            //发送请求
                            socket.setSoTimeout(Integer.MAX_VALUE);
                            outputStream = socket.getOutputStream();
                            if (outputStream != null) {
                                outputStream.write((msg1 + "\n").getBytes(StandardCharsets.UTF_8));
                                outputStream.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } else {
            logerror("服务器连接错误,即将重新连接");
            resetConnection();
        }
    }

    //重连
    public void resetConnection() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }

        if (socket != null) {
            try {
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }

        if (connectThread != null) {
            connectThread = null;
        }

        //延时连接
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*重新初始化socket*/
        if (isReConnect) {
            Thread thread = new Thread(){
                @Override
                public void run() {
                    onCreate();
                }
            };
            thread.start();
        }
    }

    //监听服务器并接受数据
    public void onListenServer() {

        if (socket != null && socket.isConnected()) {
            try {
                socket.setSoTimeout(Integer.MAX_VALUE);
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                        StandardCharsets.UTF_8));
                Log.e(TAG, "onListenServer: 开始监听服务器。。。");
                //检查是否读取到信息
                if ((response = inputStream.readLine()) != null) {
                    Log.e(TAG, "onListenServer: 读取到消息：" + response);
                    if (response.equals("ConnectionisClosed")) {
                        logerror("服务器连接关闭");
                        MainApplication.result = true;
                        resetConnection();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "onListenServer: Socket连接失败，准备重连...");
                MainApplication.result = true;
                resetConnection();
            }
        } else {
            Log.e(TAG, "onListenServer: Socket连接失败，准备重连...");
            MainApplication.result = true;
            resetConnection();
        }

        //前提是外面有Thread类来执行

    }

    private void logerror(String s) {
        Log.e(TAG, "logerror: " + s);
    }

    public String getResponse() {
        return response;
    }

}
