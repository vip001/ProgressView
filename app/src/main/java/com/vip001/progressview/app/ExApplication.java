package com.vip001.progressview.app;

import android.app.Application;

/**
 * Created by xxd on 2017/8/31.
 */

public class ExApplication extends Application {
    private static ExApplication mInstance;

    public static ExApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
}
