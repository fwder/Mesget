package com.weclont.mesget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

public class EquipmentInfoCollection {
    TelephonyManager telephonyManager;

    /**
     * 设备信息PO
     */
    private EquipmentPO equipment;

    public EquipmentPO getEquipmentInfo(Context context) {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        equipment = new EquipmentPO();
        //手机品牌
        equipment.setBrand(Build.BRAND);
        //越狱标识
        equipment.setBreakFlag(isRoot() + "");
        //手机IP
        equipment.setIp(getIPAddress(context));
        //运营商国家
        equipment.setCountry(telephonyManager.getNetworkCountryIso());
        //CPU支持的指令集
        equipment.setCpuABI(getCpuAbi());
        //CPU数量
        equipment.setCpuCount(getCPUNumCores() + "");
        //CPU型号
        equipment.setCpuHardware(getCpuName());
        //CPU序列
        equipment.setCpuSerial(getCPUSerial());
        //CPU速度
        String minCpuFreq = getMinCpuFreq();
        minCpuFreq = minCpuFreq.length() != 0 ? minCpuFreq : "0";
        String maxCpuFreq = getMaxCpuFreq();
        maxCpuFreq = maxCpuFreq.length() != 0 ? maxCpuFreq : "0";
        String curCpuFreq = getCurCpuFreq();
        curCpuFreq = curCpuFreq.length() != 0 ? curCpuFreq : "0";
        equipment.setCpuSpeed("{'cpuSpeed': { 'min':'" + minCpuFreq + "' , 'max':'" + maxCpuFreq + "' ,'cur':'" + curCpuFreq + "'}}");
        //手机设备ID
        equipment.setDeviceId(getUDID(context));
        //机型
        equipment.setModel(Build.MODEL);
        //屏幕分辨率
        equipment.setResolution(getScreenResolution(context));
        //模拟器标识
        equipment.setSimulator(isEmulator(context, telephonyManager));
        //系统总内存
        equipment.setTotalStorage(getTotalExternalMemorySize());
        //系统剩余内存
        equipment.setLeftDisk(getAvailableExternalMemorySize());
        //SD卡总内存
        equipment.setTotalMemory(getTotalInternalMemorySize());
        //SD卡可用内存;
        equipment.setLeftMemory(getAvailableInternalMemorySize());
        /**
         * 获取系统触摸屏的触摸方式
         * */
        equipment.setTouchScreen("");
        //输入法类型
        equipment.setInputMethod(getInputMethod(context));
        /**输入法版本*/
        equipment.setInputMethodVersion("");
        //时区
        equipment.setTimeZone(getCurrentTimeZone());
        //语言
        equipment.setLanguage(Locale.getDefault().getLanguage());
        //照片总数
        equipment.setDevice_pic_cnt(getPhotoNumber(context));
        //剩余电量
        equipment.setDevice_battery_level(getDevice_battery_level(context));
        return equipment;
    }

    /**
     * 判断当前手机是否有ROOT权限
     *
     * @return 0代表未越狱 1代表已越狱
     */
    private int isRoot() {
        int result = 0;
        try {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
                result = 0;
            } else {
                result = 1;
            }
            Log.d("EC.isROOT", "result = " + result);
        } catch (Exception e) {
            Log.e("isRoot", e.getMessage());
        }
        return result;
    }

    /**
     * 返回手机运营商名称
     *
     * @param telephonyManager
     * @return
     */
    private String getProvidersName(TelephonyManager telephonyManager) {
        String ProvidersName = null;
        String IMSI = telephonyManager.getSubscriberId();
        if (IMSI == null) {
            return "unknow";
        }
        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
            ProvidersName = "中国移动";
        } else if (IMSI.startsWith("46001")) {
            ProvidersName = "中国联通";
        } else if (IMSI.startsWith("46003")) {
            ProvidersName = "中国电信";
        }
        return ProvidersName;
    }

    /**
     * 获取手机IP地址
     *
     * @param context
     * @return
     */
    private String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            //当前使用2G/3G/4G网络
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    Log.e("EquipmentInfoCollection", e.getMessage());
                }
                //当前使用无线网络
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //得到IPV4地址
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
            Log.e("EC.getIPAddress", "当前无网络连接,请在设置中打开网络");
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    private String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    private final String PREFS_FILE = "gank_device_id.xml";
    private final String PREFS_DEVICE_ID = "gank_device_id";
    private String uuid;

    /**
     * 获取手机设备ID
     *
     * @param context
     * @return
     */
    private String getUDID(Context context) {
        if (uuid == null) {
            if (uuid == null) {
                SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
                String id = prefs.getString(PREFS_DEVICE_ID, null);
                if (id != null) {
                    // Use the ids previously computed and stored in the prefs file
                    uuid = id;
                } else {
                    String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    // Use the Android ID unless it's broken, in which case fallback on deviceId,
                    // unless it's not available, then fallback on a random number which we store
                    // to a prefs file
                    try {
                        if (!"9774d56d682e549c".equals(androidId)) {
                            uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8")).toString();
                        } else {
                            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                            uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")).toString() : UUID.randomUUID().toString();
                        }
                    } catch (UnsupportedEncodingException e) {
                        Log.e("EquipmentInfoCollection", e.getMessage());
                    }
                    // Write the value out to the prefs file
                    prefs.edit().putString(PREFS_DEVICE_ID, uuid).commit();
                }
            }
        }
        return uuid;
    }

    /**
     * 获取手机cpu架构，支持的指令集
     *
     * @return
     */
    private String getCpuAbi() {
        String[] abis = new String[]{};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            abis = Build.SUPPORTED_ABIS;
        } else {
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
        StringBuilder abiStr = new StringBuilder();
        for (String abi : abis) {
            abiStr.append(abi);
            abiStr.append(',');
        }
        return abiStr.toString();
    }

    /**
     * 获取CPU个数
     *
     * @return
     */
    private int getCPUNumCores() {
        /**Private Class to display only CPU devices in the directory listing*/
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }
        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            // Default to return 1 core
            Log.e("EquipmentInfoCollection", e.getMessage());
            return 1;
        }
    }

    /**
     * 获取CPU型号
     *
     * @return
     */
    private String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            Log.e("EquipmentInfoCollection", e.getMessage());
        } catch (IOException e) {
            Log.e("EquipmentInfoCollection", e.getMessage());
        }
        return null;
    }

    /**
     * 获取CPU最大频率
     *
     * @return
     */
    private String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            Log.e("EquipmentInfoCollection", ex.getMessage());
            result = "";
        }
        return result.trim();
    }

    /**
     * 获取CPU最小频率
     *
     * @return
     */
    private String getMinCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "";
        }
        return result.trim();
    }

    /**
     * 实时获取CPU当前频率（单位KHZ）
     *
     * @return
     */
    private static String getCurCpuFreq() {
        String result = "";
        try {
            FileReader fr = new FileReader(
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim();
        } catch (FileNotFoundException e) {
            Log.e("EquipmentInfoCollection", e.getMessage());
        } catch (IOException e) {
            Log.e("EquipmentInfoCollection", e.getMessage());
        }
        return result;
    }

    /**
     * 获取CPU序列号
     *
     * @return CPU序列号(16位)
     * <p>
     * 读取失败为"0000000000000000"
     */

    private String getCPUSerial() {
        String str = "", strCPU = "", cpuAddress = "";
        try {
            //读取CPU信息
            Process pp = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            //查找CPU序列号
            for (int i = 1; i < 100; i++) {
                str = input.readLine();
                if (str != null) {
                    //查找到序列号所在行
                    if (str.indexOf("Serial") > -1) {
                        //提取序列号
                        strCPU = str.substring(str.indexOf(":") + 1, str.length());
                        //去空格
                        cpuAddress = strCPU.trim();
                        break;
                    }
                } else {
                    //文件结尾
                    break;
                }
            }
        } catch (IOException ex) {
            //赋予默认值
            Log.e("EquipmentInfoCollection", ex.getMessage());
        }
        return cpuAddress;
    }

    /**
     * 获取屏幕分辨率
     *
     * @param context
     * @return
     */
    private String getScreenResolution(Context context) {
        // 通过Resources获取
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;
        return "{'resolution': { 'height':" + dm.heightPixels + " , 'width':" + dm.widthPixels + " ,'dpi':" + densityDpi + "}}";
    }

    private String[] known_qemu_drivers = {"goldfish"};

    /**
     * 判断是否是模拟器运行
     *
     * @return
     */
    private boolean isEmulator(Context context, TelephonyManager telephonyManager) {
        try {
            String imei = telephonyManager.getDeviceId();
            if (imei != null && imei.equals("000000000000000")) {
                return true;
            }
            return (Build.MODEL.equals("sdk"))
                    || (Build.MODEL.equals("google_sdk"));
        } catch (Exception ioe) {
            Log.w("PhoneHelper", "009:" + ioe.toString());
        }
        return false;
    }

    DecimalFormat df = new DecimalFormat("#.##");

    /**
     * 获取手机剩余内存
     *
     * @return
     */
    private String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return df.format(availableBlocks * blockSize / (double) (1024 * 1024)) + "MB";
    }

    /**
     * 获取默认输入法
     *
     * @param context
     * @return
     */
    private String getInputMethod(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> methodList = imm.getInputMethodList();
        //默认输入法在list中为第一个数据
        CharSequence charSequence = methodList.get(0).loadLabel(context.getPackageManager());
        return charSequence.toString();
    }

    /**
     * 获取手机总内存
     *
     * @return
     */
    private String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return df.format(totalBlocks * blockSize / (double) (1024 * 1024)) + "MB";
    }

    private boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡可用内存
     *
     * @return
     */
    private String getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return df.format(availableBlocks * blockSize / (double) (1024 * 1024 * 1024)) + "GB";
        } else {
            return "";
        }
    }

    /**
     * 获取SD卡总内存
     *
     * @return
     */
    private String getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return df.format(totalBlocks * blockSize / (double) (1024 * 1024 * 1024)) + "GB";
        } else {
            return "";
        }
    }


    /**
     * 获取GTM格式的时区
     *
     * @return
     */
    private String getCurrentTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        return createGmtOffsetString(true, true, tz.getRawOffset());
    }

    private String createGmtOffsetString(boolean includeGmt,
                                         boolean includeMinuteSeparator, int offsetMillis) {
        int offsetMinutes = offsetMillis / 60000;
        char sign = '+';
        if (offsetMinutes < 0) {
            sign = '-';
            offsetMinutes = -offsetMinutes;
        }
        StringBuilder builder = new StringBuilder(9);
        if (includeGmt) {
            builder.append("GMT");
        }
        builder.append(sign);
        appendNumber(builder, 2, offsetMinutes / 60);
        if (includeMinuteSeparator) {
            builder.append(':');
        }
        appendNumber(builder, 2, offsetMinutes % 60);
        return builder.toString();
    }

    private static void appendNumber(StringBuilder builder, int count, int value) {
        String string = Integer.toString(value);
        for (int i = 0; i < count - string.length(); i++) {
            builder.append('0');
        }
        builder.append(string);
    }

    /**
     * 判断设备是否支持多点触控
     *
     * @param context
     * @return
     */
    private static boolean isSupportMultiTouch(Context context) {
        /**
         * FEATURE_TOUCHSCREEN_MULTITOUCH表示:该设备的触摸屏支持多点触控足够的基本两个手指的手势检测。
         * FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT表示:该设备的触摸屏是能够跟踪两个或两个以上的手指完全独立。
         * FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND表示:设备的触摸屏是能够跟踪满手的手指完全独立的 - 这是5或更多的同步独立的指针。
         */
        PackageManager pm = context.getPackageManager();
        boolean isSupportMultiTouch = pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
        return isSupportMultiTouch;
    }

    /**
     * 获取手机里照片总数
     *
     * @param context
     * @return
     */
    private Integer getPhotoNumber(Context context) {
        int i = 0;
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            //获取图片的名称  获取图片的名称
//            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            //获取图片的生成日期
//            byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            //获取图片的详细信息
//            String desc = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
            i++;
        }
        cursor.close();
        return i;
//                File file = new File(string);
//                File[] files = file.listFiles();
//                for (int j = 0; j < files.length; j++) {
//                    String name = files[j].getName();
//                    if (files[j].isDirectory()) {
//                        String dirPath = files[j].toString().toLowerCase();
//                        System.out.println(dirPath);
//                        getPhotoNumber(dirPath + "/");
//                    } else if (files[j].isFile() & name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".gif") || name.endsWith(".jpeg")) {
//                        System.out.println("FileName===" + files[j].getName());
//                        i++;
//                    }
//                }
    }

    private float getDevice_battery_level(Context context) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        };
        Intent batteryInfoIntent = context.registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //电量（0-100）
        int level = batteryInfoIntent.getIntExtra("level", 0);
//        int status = batteryInfoIntent.getIntExtra( "status" , 0 );
//        int health = batteryInfoIntent.getIntExtra( "health" , 1 );
//        boolean present = batteryInfoIntent.getBooleanExtra( "present" , false );
//        int scale = batteryInfoIntent.getIntExtra( "scale" , 0 );
//        int plugged = batteryInfoIntent.getIntExtra( "plugged" , 0 );
//        //电压
//        int voltage = batteryInfoIntent.getIntExtra( "voltage" , 0 );
//        // 温度的单位是10℃
//        int temperature = batteryInfoIntent.getIntExtra( "temperature" , 0 );
//        String technology = batteryInfoIntent.getStringExtra( "technology" );
        context.unregisterReceiver(broadcastReceiver);
        return level;
    }

}

