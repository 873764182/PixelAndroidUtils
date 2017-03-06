package com.pixel.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by pixel on 2017/3/6.
 * <p>
 * 提供与系统相关的工具对象
 */

public abstract class SystemUtil {
    private static final String TAG = "SystemUtil";

    /**
     * 压缩目录为压缩包(zip)
     *
     * @param inputDirectoryFile 压缩目录
     * @param outZipName         输出文件(.zip后缀)
     * @throws IOException
     */
    public static void doZipCompress(File inputDirectoryFile, String outZipName) throws IOException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outZipName));
        BufferedOutputStream bo = new BufferedOutputStream(out);
        doZipCompress(out, inputDirectoryFile, inputDirectoryFile.getName(), bo);
        bo.close();
        out.close(); // 输出流关闭
    }

    public static void doZipCompress(ZipOutputStream zipOutputStream, File file, String fileName, BufferedOutputStream bufferedOutputStream) throws IOException { // 方法重载
        if (file.isDirectory()) {
            File[] fl = file.listFiles();
            if (fl.length == 0) {
                zipOutputStream.putNextEntry(new ZipEntry(fileName + "/")); // 创建zip压缩进入点base(空文件夹是中文名时会异常)
            }
            for (int i = 0; i < fl.length; i++) {
                doZipCompress(zipOutputStream, fl[i], fileName + "/" + fl[i].getName(), bufferedOutputStream); // 递归遍历子文件夹
            }
        } else {
            zipOutputStream.putNextEntry(new ZipEntry(fileName)); // 创建zip压缩进入点base
            FileInputStream in = new FileInputStream(file);
            BufferedInputStream bi = new BufferedInputStream(in);
            int b;
            while ((b = bi.read()) != -1) {
                bufferedOutputStream.write(b); // 将字节流写入当前zip目录
            }
            bufferedOutputStream.flush();
            zipOutputStream.flush();
            bi.close();
            in.close(); // 输入流关闭
        }
    }

    /**
     * ZIP压缩包解压
     *
     * @param zipPath      源文件全路径
     * @param outDirectory 解压输出目录
     */
    public static void doZipDecompress(String zipPath, String outDirectory) throws IOException {
        ZipInputStream zin = new ZipInputStream(new FileInputStream(zipPath));  // 输入源zip路径
        BufferedInputStream bin = new BufferedInputStream(zin);

        File file = null;
        ZipEntry entry = null;
        while ((entry = zin.getNextEntry()) != null) {

            if (entry.isDirectory()) {
                file = new File(outDirectory + "\\" + entry.getName());
                file.mkdirs();
                continue;   // 文件夹停止执行
            }

            file = new File(outDirectory, entry.getName());
            if (!file.exists()) {
                (new File(file.getParent())).mkdirs();  // 创建文件对应的目录
            }

            FileOutputStream out = new FileOutputStream(file);
            BufferedOutputStream bout = new BufferedOutputStream(out);
            int b;
            while ((b = bin.read()) != -1) {
                bout.write(b);
            }

            bout.flush();
            out.flush();
            bout.close();
            out.close();
        }
        bin.close();
        zin.close();
    }

    /**
     * 使用GZIP实现对单个文件的压缩处理
     *
     * @param inputPath 原始文件
     * @param outPath   压缩输出文件(.zip.gz后缀)
     * @param encoding  字符编码
     * @throws IOException
     */
    public static void doCompressGZIP(String inputPath, String outPath, String encoding) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath), encoding));
        //使用GZIPOutputStream包装OutputStream流，使其具体压缩特性，最后会生成.gz压缩包.
        BufferedOutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outPath)));
        int c;
        while ((c = in.read()) != -1) {
            // 注，这里是压缩一个字符文件，前面是以字符流来读的，不能直接存入c，因为c已是Unicode码，这样会丢掉信息的（当然本身编码格式就不对），所以这里要以GBK来解后再存入。
            out.write(String.valueOf((char) c).getBytes(encoding));
        }
        out.flush();
        in.close();
        out.close();
    }

    /**
     * 读取GZIP文件内容
     *
     * @param gzipFilePath gzip文件路径
     * @param encoding     字符编码
     * @return
     * @throws IOException
     */
    public static String doZipDecompressGZIP(String gzipFilePath, String encoding) throws IOException {
        //使用GZIPInputStream包装InputStream流，使其具有解压特性
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(gzipFilePath)), encoding));
        StringBuilder stringBuilder = new StringBuilder();
        String string;
        while ((string = bufferedReader.readLine()) != null) {  // 读取压缩文件里的内容
            stringBuilder.append(string);
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    /**
     * 获取文件摘要
     *
     * @param file      要获取摘要的文件
     * @param algorithm 算法: MD5, SHA1, SHA-256, SHA-384, SHA-512
     * @return
     * @throws Exception
     */
    public static String getFileDigest(File file, String algorithm) throws Exception {
        if (!file.isFile()) return null;
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        digest.reset();
        FileInputStream in = new FileInputStream(file);
        byte buffer[] = new byte[1024];
        int len = 0;
        while ((len = in.read(buffer, 0, 1024)) != -1) {
            digest.update(buffer, 0, len);
        }
        in.close();
        return new BigInteger(1, digest.digest()).toString(16);
    }

    /**
     * 获取目录下所有文件的摘要集合
     *
     * @param directory 要获取摘要的目录
     * @param algorithm 算法: MD5, SHA1, SHA-256, SHA-384, SHA-512
     * @param recursive 是否递归子目录的文件
     * @return
     * @throws Exception
     */
    public static Map<String, String> getDirectoryAllFileDigest(File directory, String algorithm, boolean recursive) throws Exception {
        Map<String, String> digestMap = new HashMap<>();
        if (!directory.isDirectory()) return digestMap;
        File files[] = directory.listFiles();
        for (File file : files) {
            if (recursive && file.isDirectory()) {
                digestMap.putAll(getDirectoryAllFileDigest(file, algorithm, recursive));
            } else {
                String digest = getFileDigest(file, algorithm);
                if (digest != null) {
                    digestMap.put(file.getPath(), digest);
                }
            }
        }
        return digestMap;
    }

    /**
     * 获取程序进程名称
     *
     * @return 进程名称
     */
    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessInfoList) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 获取设备IMEI（ 国际移动设备识别码）如果手机打开了飞行模式 有可能读取到的是 MEID (CDMA制式的设备ID)
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceIMEI(Context context) {
        try {
            return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断程序是否在前台运行
     *
     * @param packageName 程序包名
     */
    public static boolean appIsInFront(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runinfo : runningAppProcesses) {
            if (runinfo.processName.equals(packageName) && runinfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取正在运行的Activity类全名
     * 需要权限
     * <uses-permission android:name="android.permission.GET_TASKS"/>
     */
    public static String getFrontActivityName(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo cinfo = runningTasks.get(0);
        ComponentName component = cinfo.topActivity;
        return component.getClassName();
    }

    /**
     * 获取程序版本名称
     */
    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 获取程序版本号
     */
    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    /**
     * 检查手机上是否安装了指定的软件
     */
    public static boolean isInstallApk(Context context, String packageName) {
        //获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(0);
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                if (packageName.equalsIgnoreCase(packageInfos.get(i).packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取应用程序信息(也可以判断应用程序是否已安装,获取自己传入context.getPackageName().)
     */
    public static ApplicationInfo getApplicationInfo(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 安装APK文件
     */
    public static void installApk(Context context, String apkUrl) {
        if (apkUrl == null || apkUrl.length() <= 0) {
            throw new NullPointerException("APK路径不能为空");
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (apkUrl.startsWith("file:")) {
            i.setDataAndType(Uri.parse(apkUrl.toString()), "application/vnd.android.package-archive");
        } else {
            i.setDataAndType(Uri.fromFile(new File(apkUrl)), "application/vnd.android.package-archive");
        }
        context.startActivity(i);
    }

    /**
     * 调用系统已安装程序打开文件(也可以拿来安装APK)
     */
    public static void openFile(Context context, File file) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), getFileMime(file));
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "你手机没有安装可以打开该附件的应用", e);
        } catch (Error error) {
            Log.e(TAG, "打开文件失败 " + error.getMessage());
        }
    }

    /**
     * 获得文件对应的MIME类型(根据后缀名)
     */
    public static String getFileMime(File file) {
        String fName = file.getName();  /* 获取后缀名前的分隔符"."在fName中的位置 */
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex <= 0) {
            return "*/*";
        }
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();   /* 获取文件的后缀名 且转为小写*/
        if (end.length() <= 0) {
            return "*/*";
        }
        return MIME_MAP.get(end);   /*在MIME和文件类型的匹配表中找到对应的MIME类型*/
    }

    /**
     * {后缀名，MIME类型}
     */
    static final Map<String, String> MIME_MAP = new HashMap<String, String>() {
        {
            put(".amr", "audio/amr");
            put(".3gp", "video/3gpp");
            put(".apk", "application/vnd.android.package-archive");
            put(".asf", "video/x-ms-asf");
            put(".avi", "video/x-msvideo");
            put(".bin", "application/octet-stream");
            put(".bmp", "image/bmp");
            put(".c", "text/plain");
            put(".class", "application/octet-stream");
            put(".conf", "text/plain");
            put(".cpp", "text/plain");
            put(".doc", "application/msword");
            put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            put(".xls", "application/vnd.ms-excel");
            put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            put(".exe", "application/octet-stream");
            put(".gif", "image/gif");
            put(".gtar", "application/x-gtar");
            put(".gz", "application/x-gzip");
            put(".h", "text/plain");
            put(".htm", "text/html");
            put(".html", "text/html");
            put(".jar", "application/java-archive");
            put(".java", "text/plain");
            put(".jpeg", "image/jpeg");
            put(".jpg", "image/jpeg");
            put(".js", "application/x-javascript");
            put(".log", "text/plain");
            put(".m3u", "audio/x-mpegurl");
            put(".m4a", "audio/mp4a-latm");
            put(".m4b", "audio/mp4a-latm");
            put(".m4p", "audio/mp4a-latm");
            put(".m4u", "video/vnd.mpegurl");
            put(".m4v", "video/x-m4v");
            put(".mov", "video/quicktime");
            put(".mp2", "audio/x-mpeg");
            put(".mp3", "audio/x-mpeg");
            put(".mp4", "video/mp4");
            put(".mpc", "application/vnd.mpohun.certificate");
            put(".mpe", "video/mpeg");
            put(".mpeg", "video/mpeg");
            put(".mpg", "video/mpeg");
            put(".mpg4", "video/mp4");
            put(".mpga", "audio/mpeg");
            put(".msg", "application/vnd.ms-outlook");
            put(".ogg", "audio/ogg");
            put(".pdf", "application/pdf");
            put(".png", "image/png");
            put(".pps", "application/vnd.ms-powerpoint");
            put(".ppt", "application/vnd.ms-powerpoint");
            put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
            put(".prop", "text/plain");
            put(".rc", "text/plain");
            put(".rmvb", "audio/x-pn-realaudio");
            put(".rtf", "application/rtf");
            put(".sh", "text/plain");
            put(".tar", "application/x-tar");
            put(".tgz", "application/x-compressed");
            put(".txt", "text/plain");
            put(".wav", "audio/x-wav");
            put(".wma", "audio/x-ms-wma");
            put(".wmv", "audio/x-ms-wmv");
            put(".wps", "application/vnd.ms-works");
            put(".xml", "text/plain");
            put(".z", "application/x-compress");
            put(".zip", "application/x-zip-compressed");
            put("", "*/*");
        }
    };

    /**
     * 判断当前设备是手机还是平板 平板返回 True，手机返回 False
     */
    public static boolean deviceIsTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * SD卡是否可用
     */
    public static boolean sdIsAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取内置SD卡路径 storage/sdcard/YDBGXT
     */
    public static String getSdPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 获取程序DATA(数据)目录
     */
    public static String getDataPath() {
        return Environment.getDataDirectory().getPath();
    }

    /**
     * 获取系统公共储存目录
     *
     * @param type # DIRECTORY_MUSIC
     *             # DIRECTORY_PODCASTS
     *             # DIRECTORY_RINGTONES
     *             # DIRECTORY_ALARMS
     *             # DIRECTORY_NOTIFICATIONS
     *             # DIRECTORY_PICTURES
     *             # DIRECTORY_MOVIES
     *             # DIRECTORY_DOWNLOADS
     *             # DIRECTORY_DCIM
     * @return 路径
     */
    public static String getPublicPath(String type) {
        return Environment.getExternalStoragePublicDirectory(type).getPath();
    }

    /**
     * 创建目录
     */
    public static boolean createFolder(String folderName) {
        if (!sdIsAvailable()) {
            throw new NullPointerException("存储卡不可用");
        }
        File dir = new File(getSdPath() + "/" + folderName);
        if (!dir.exists() || dir.isFile()) {
            return dir.mkdirs();
        }
        return false;
    }

    /**
     * 删除SD卡文件和文件夹
     */
    public static boolean deleteFile(File file) {
        if (file.isFile()) {
            return file.delete();
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return file.delete();
            }
            for (int i = 0; i < childFiles.length; i++) {
                deleteFile(childFiles[i]);
            }
            return file.delete();
        }
        return false;
    }

    /**
     * 隐藏键盘
     */
    public static boolean hiddenKeyboard(Context context, View view) {
        return ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 显示键盘
     */
    public static boolean showKeyboard(Context context, EditText editText) {
        return ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, InputMethodManager.RESULT_SHOWN);
    }

    /**
     * 重启APP
     */
    public static void restartApp(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    /**
     * 任务管理器的方式退出当前程序
     * 需求权限：android.Manifest.permission.KILL_BACKGROUND_PROCESSES
     */
    public static void killApp(Context context) {
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).killBackgroundProcesses(context.getPackageName());
    }

    /**
     * Uri获取路径
     */
    public static String getPathByUri(Context context, Uri uri) {
        String filename = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {  // 4.4及以上系统
            filename = GetPathFromUri4kitkat.getPath(context, uri);
        } else {
            Cursor cursor = null;
            if (uri.getScheme().toString().compareTo("content") == 0) {
                cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Audio.Media.DATA}, null, null, null);
                if (cursor.moveToFirst()) {
                    filename = cursor.getString(0);
                }
            } else if (uri.getScheme().toString().compareTo("file") == 0) {             //file:///开头的uri
                filename = uri.toString();
                filename = uri.toString().replace("file://", "");
                if (!filename.startsWith("/mnt")) {  //替换file://
                    filename += "/mnt";//加上"/mnt"头
                }
            }
        }
        return filename;
    }

    //<editor-fold desc="根据URI获取path,适配5.0和以上系统">
    static class GetPathFromUri4kitkat {

        // 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
        @SuppressLint("NewApi")
        public static String getPath(final Context context, final Uri uri) {
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
            return null;
        }

        public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(column_index);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return null;
        }

        public static boolean isExternalStorageDocument(Uri uri) {
            return "com.android.externalstorage.documents".equals(uri.getAuthority());
        }

        public static boolean isDownloadsDocument(Uri uri) {
            return "com.android.providers.downloads.documents".equals(uri.getAuthority());
        }

        public static boolean isMediaDocument(Uri uri) {
            return "com.android.providers.media.documents".equals(uri.getAuthority());
        }
    }
    //</editor-fold>

}
