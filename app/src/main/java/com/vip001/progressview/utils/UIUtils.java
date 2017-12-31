package com.vip001.progressview.utils;

import android.util.DisplayMetrics;

import com.vip001.progressview.app.ExApplication;

/**
 * Created by xxd on 2017/8/31.
 */

public class UIUtils {
    public static float DENSITY;
    public static int SCREENHEIGHT;
    public static int SCREENWIDTH;
    public static float SCALEDENSITY;

    static {
        DisplayMetrics metrics = ExApplication.getInstance().getResources().getDisplayMetrics();
        DENSITY = metrics.density;
        SCREENWIDTH = metrics.widthPixels;
        SCREENHEIGHT = metrics.heightPixels;
        SCALEDENSITY = metrics.scaledDensity;
    }

    public static int dip2ipx(float dp) {
        return (int) (DENSITY * dp + 0.5);
    }
    public static float dip2fpx(float dp){
        return  DENSITY*dp;
    }

    public static float px2fdip(float px) {

        return px / DENSITY;
    }
    public static  int px2idip(float px){
        return (int) (px/DENSITY+0.5);
    }
}
