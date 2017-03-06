package com.pixel.utils;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pixel on 2017/3/6.
 * <p>
 * 提供与网络相关的工具
 */

public abstract class NetworkUtil {
    private static final String TAG = "NetworkUtil";

    public interface OnDownCallBack {
        void onProgress(long downSize, long fileSize, boolean complete);

        void onError(Exception e);
    }

    public interface OnRequestCallBack {
        void onString(String string);

        void onError(Exception e);
    }

    private static void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /**
     * 获取链接对象
     */
    public static HttpURLConnection getConnection(String downUrl, String method) throws Exception {
        URL url = new URL(downUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Accept-Language", "zh_CN");    // 语言
        conn.setRequestProperty("Charset", "UTF-8");                // 字符
        return conn;
    }

    /**
     * 发送GET请求
     */
    public static void get(final String url, final OnRequestCallBack onRequestCallBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                try {
                    inputStream = getConnection(url, "GET").getInputStream();
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i = -1;
                    while ((i = inputStream.read()) != -1) {
                        baos.write(i);
                    }
                    if (onRequestCallBack == null) return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onRequestCallBack.onString(baos.toString());
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onRequestCallBack.onError(e);
                        }
                    });
                } finally {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 下载文件
     *
     * @param downUrl        下载地址
     * @param savePath       保存地址
     * @param onDownCallBack 回调进度
     */
    public static void down(final String downUrl, final String savePath, final OnDownCallBack onDownCallBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RandomAccessFile currentPart = null;
                InputStream inputStream = null;
                try {
                    long downSize = 0L;
                    currentPart = new RandomAccessFile(savePath, "rw");
                    final long fileSize = getConnection(downUrl, "GET").getContentLength();    // 获取大小是单次请求
                    HttpURLConnection connection = getConnection(downUrl, "GET");
                    inputStream = connection.getInputStream();
                    byte[] buffer = new byte[1024];
                    int hasRead = -1;
                    while ((hasRead = inputStream.read(buffer)) != -1) {
                        currentPart.write(buffer, 0, hasRead);
                        downSize += hasRead;
                        if (onDownCallBack != null) {
                            onDownCallBack.onProgress(downSize, fileSize, false);
                        }
                    }
                    if (onDownCallBack != null)
                        onDownCallBack.onProgress(downSize, fileSize, true); //
                } catch (Exception e) {
                    e.printStackTrace();
                    if (onDownCallBack != null)
                        onDownCallBack.onError(e);
                } finally {
                    try {
                        currentPart.close();
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
