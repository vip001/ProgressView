package com.vip001.progressview.view;

/**
 * Created by vip001 on 2017/9/11.
 */

public interface ProgressControl {


    /**
     * 开始加速
     */
    void startGradientAnimation();

    /**
     * 停止加速
     */
    void stopGradientAnimation();

    /**
     * 暂停
     */
    void pause();

    /**
     * 恢复
     */
    void resume();

    /**
     * 同步进度
     * @param progress
     */
    void syncProgress(int progress);

    /**
     * 异步进度
     * @param progress
     */
    void asyncProgress(final int progress);

    /**
     * 设置进度条最大值
     * @param max
     */
    void setMax(int max);

    /**
     * 判断是否在做光带动画
     * @return
     */
    boolean isGradientAnimated();

    /**
     * 暂停状态判断
     * @return
     */
    boolean isPause();

    /**
     * 隐藏进度条
     */
    void hideProgresss();

    /**
     * 展示进度条
     */
    void showProgress();

    /**
     * 添加key
     * @param key
     */
    void setSaveKey(String key);

    /**
     * 获取保存的key
     * @return
     */
    String getSaveKey();

    /**
     * 获取自己的引用
     * @return
     */
    ProgressControl getProgressControl();
}
