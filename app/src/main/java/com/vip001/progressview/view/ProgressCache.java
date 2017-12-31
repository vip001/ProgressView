package com.vip001.progressview.view;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vip001 on 2017/9/12.
 * 此类用于保存下载页面进度条加速点的数据
 */

public class ProgressCache {
    private static HashMap<String, ArrayList<Float>> sCache = new HashMap<String, ArrayList<Float>>();

    /**
     * 保存加速点数据
     * @param key
     * @param value
     */
    public static void save(String key, float value) {
        ArrayList<Float> al = read(key);
        al.add(value);
    }

    /**
     * 读取缓存里的加速点数据
     * @param key
     * @return
     */
    public static ArrayList<Float> read(String key) {
        ArrayList<Float> al = sCache.get(key);
        if (al == null) {
            al = new ArrayList<>();
            sCache.put(key, al);
        }
        return al;
    }

    /**
     * 清除加速点数据
     * @param key
     * @return
     */
    public static ArrayList<Float> clear(String key) {
        return sCache.remove(key);
    }

    /**
     * 打印进度点数据
     * @param key
     * @return
     */
    public static String printProgressData(String key) {
        ArrayList<Float> al = sCache.get(key);
        StringBuilder stringBuilder = new StringBuilder();
        if (al != null) {
            for (int i = 0, len = al.size(); i < len; i++) {
                stringBuilder.append(al.get(i));
                stringBuilder.append(";");
            }
        }
        return stringBuilder.toString();
    }

}
