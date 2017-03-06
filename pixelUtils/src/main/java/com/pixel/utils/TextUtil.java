package com.pixel.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pixel on 2017/3/6.
 * <p>
 * 提供与文本相关的工具
 */

public abstract class TextUtil {
    private static final String TAG = "TextUtil";

    /**
     * 字符串非空判断
     */
    public static boolean notNull(String string) {
        if (string != null && string.length() > 0 || !"null".equalsIgnoreCase(string)) {
            return true;
        }
        return false;
    }

    /**
     * 对比版本号 新版本大于旧版本 返回 true
     */
    public static boolean contrastVersion(String oldVersion, String newVersion) {
        if (!notNull(oldVersion) || !notNull(newVersion)) {
            return false;
        }
        oldVersion = "1" + removeChar(oldVersion).replace(".", "");
        newVersion = "1" + removeChar(newVersion).replace(".", "");
        int oldVersionLength = oldVersion.length();
        int newVersionLength = newVersion.length();
        if (oldVersionLength != newVersionLength) {
            if (oldVersionLength > newVersionLength) {  // 匹配长度
                for (int i = 0; i < oldVersionLength - newVersionLength; i++) {
                    newVersion += "0";
                }
            } else {
                for (int i = 0; i < newVersionLength - oldVersionLength; i++) {
                    oldVersion += "0";
                }
            }
        }
        int oldVersionNumber = Integer.valueOf(oldVersion);
        int newVersionNumber = Integer.valueOf(newVersion);
        if (newVersionNumber > oldVersionNumber) {
            return true;
        }
        return false;
    }

    /**
     * 去除非数字的字符串
     */
    public static String removeChar(String string) {
        char[] charArray = string.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9') {
                // 是数字
            } else {
                charArray[i] = 'c';
            }
        }
        return Arrays.toString(charArray).replace("c", "").replace("[", "").replace("]", "").replace(",", "").replace(" ", "");
    }

    /**
     * 判断一个字符串是否可以转为数值
     */
    public static boolean isNumber(String string) {
        if (string == null || string.length() <= 0) {
            return false;
        }
        char[] charArray = string.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9') {
                // 是数字
            } else {
                return false;
            }
        }
        return true;
    }

    //判断email格式是否正确
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 去掉html标签
     */
    public static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
        String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
        String regEx_space = "\\s*|\t|\r|\n";//定义空格回车换行符

        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); // 过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); // 过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); // 过滤html标签

        Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(htmlStr);
        htmlStr = m_space.replaceAll(""); // 过滤空格回车标签
        return htmlStr.trim().replaceAll("&nbsp;", " ").replace("\">", ""); // 返回文本字符串
    }

    /**
     * 首字符空两格
     */
    public static String addLastBlank(String string) {
        if (notNull(string)) {
            if (string.startsWith("\n")) {
                string = string.substring("\n".length(), string.length());
            }
            if (!string.startsWith("\u3000\u3000")) {
                string = "\u3000\u3000" + string.replace(" ", "");
            }
        }
        return string;
    }

    /**
     * 根据路径获取文件名
     */
    public static String getNameByPath(String path) {
        if (notNull(path)) {
            return path.substring(path.lastIndexOf("/") + 1, path.length());
        }
        return null;
    }

    /**
     * 从字符串后面删除指定长度的字符
     */
    public static String removeCharByLast(String string, int size) {
        return string.substring(0, string.length() - size);
    }

    /**
     * 获取字符串MD5
     */
    public static String getStringMD5(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            byte stringBytes[] = string.getBytes();
            digest.update(stringBytes, 0, stringBytes.length);
            return new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

}
