package com.pixel.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

/**
 * Created by pixel on 2017/3/6.
 * <p>
 * 提供与界面相关的工具对象
 */

public abstract class UIUtil {
    private static final String TAG = "UIUtil";

    /**
     * dp转px
     */
    public static float dpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    /**
     * px转dp
     */
    public static float pxToDp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    /**
     * 获取分辨率
     * displayMetrics.density; // 得到密度
     * displayMetrics.widthPixels;// 得到宽度
     * displayMetrics.heightPixels;// 得到高度
     */
    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    /**
     * 获取 状态栏 高度
     */
    public static int getStateViewHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    /**
     * 获取 标题栏 高度
     */
    public static int getTitleViewHeight(Activity activity) {
        return activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop() - getStateViewHeight(activity);
    }

    /**
     * 获取ListView滚动距离
     */
    public static int getListViewScrollY(ListView listView) {
        View c = listView.getChildAt(0);
        if (c == null) return 0;
        int firstVisiblePosition = listView.getFirstVisiblePosition();  // 获取第一个可见Item的下标
        int top = c.getTop();
        return firstVisiblePosition * c.getHeight() - top;
    }

}
