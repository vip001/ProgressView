package com.vip001.progressview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.xunlei.progressview.R;
import com.vip001.progressview.utils.UIUtils;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by vip001 on 2017/8/31.
 */

public class FlickerSectionProgressView extends View implements ProgressControl {
    public Paint mBackgroundPaint = null;
    private Paint mEffectPaint = null;
    private Paint mCommonProgressPaint = null;
    private Paint mEffectProgressPaint = null;
    private Paint mPausePaint;
    private Matrix mGradientMatrix;
    Bitmap mCacheBitmap = null;
    Canvas mCacheCanvas = null;
    private float mViewWidth;
    private float mViewHeight;
    private LinearGradient mProgressGradient;
    private boolean needUpdateGradient = false;
    private boolean isEffectAnimated;
    private boolean canUpdateProgressData;
    private boolean isPause;
    private float mTranslateX;
    private int mProgressMax = 100;
    private int mEffectProgressColor = Color.parseColor("#ff9600");
    private float mRate = 0;
    private ArrayList<Float> mProgressData;
    private Bitmap mRocketBitmap;
    private int mProgressTop;
    private int mProgressHeight;
    private int mRocketTailerWidth;
    private int mMultipleParams = 1;
    private String mKey = null;
    private static final float MAX_TRANSLATE;
    private static final float MIN_TRANSLATE;
    private static final float MIDDLE_TRANSLATE;
    private static final float LOW_TRANSLATE;
    private static final float MIN_GRADIENTWIDTH;
    private long mLastTime;
    static {
        MIN_TRANSLATE = UIUtils.dip2fpx( 1);
        MAX_TRANSLATE = UIUtils.dip2fpx(3f);
        MIDDLE_TRANSLATE = UIUtils.dip2fpx( 2.5f);
        LOW_TRANSLATE = UIUtils.dip2fpx( 2f);
        MIN_GRADIENTWIDTH = UIUtils.dip2fpx( 4f);
    }

    public FlickerSectionProgressView(Context context) {
        super(context);
        initView();
    }


    public FlickerSectionProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FlickerSectionProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(21)
    public FlickerSectionProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.parseColor("#efeff0"));
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setAntiAlias(true);
        mEffectPaint = new Paint();
        mEffectPaint.setStyle(Paint.Style.FILL);
        mEffectPaint.setAntiAlias(true);
        mCommonProgressPaint = new Paint();
        mCommonProgressPaint.setColor(Color.parseColor("#1294f6"));
        mCommonProgressPaint.setStyle(Paint.Style.FILL);
        mCommonProgressPaint.setAntiAlias(true);
        mEffectProgressPaint = new Paint();
        mEffectProgressPaint.setColor(mEffectProgressColor);
        mEffectProgressPaint.setStyle(Paint.Style.FILL);
        mEffectProgressPaint.setAntiAlias(true);
        mPausePaint = new Paint();
        mPausePaint.setAntiAlias(true);
        mPausePaint.setColor(Color.parseColor("#cfd0d5"));
        isPause = false;
        canUpdateProgressData = true;
        mProgressData = new ArrayList<Float>();
        mGradientMatrix = new Matrix();
        mRocketBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.rocket);
        mProgressTop = UIUtils.dip2ipx( 3 * mMultipleParams);
        mRocketTailerWidth =UIUtils.dip2ipx(  2.0f / 3.0f * mMultipleParams);
        mProgressHeight =UIUtils.dip2ipx(  2 * mMultipleParams);

        mKey= UUID.randomUUID().toString();
    }

    private float getEffectLeft() {
        float rate = mProgressData.size() > 0 ? mProgressData.get(mProgressData.size() - 1) : 0;
        return getRateProgressWidth(rate);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mCacheBitmap == null || mCacheBitmap.getWidth() != w || mCacheBitmap.getHeight() != h) {
            mViewWidth = w>0?w:1;
            mViewHeight = h>0?h:1;
            mCacheBitmap = Bitmap.createBitmap( w>0?w:1,h>0?h:1, Bitmap.Config.ARGB_8888);
            mCacheCanvas = new Canvas();
            mCacheCanvas.setBitmap(mCacheBitmap);
            needUpdateGradient = true;
            renewProgressGradient();
        }

        //startGradientAnimation();
    }

    private boolean checkProgressValid(int progress) {
        return progress >= 0 && progress <= mProgressMax;
    }

    private void readPoints() {
        ArrayList<Float> data = ProgressCache.read(mKey);
        if (data.size() != mProgressData.size()) {
            mProgressData = data;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        readPoints();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // clearGradienAnimation();
    }

    private float getProgressGradientWidth() {
        return mViewWidth * mRate * 0.3f;
    }

    private float getRateProgressWidth() {
        float value = mProgressData.size() > 0 ? getProgressWidth() - getEffectLeft() : 0;
        if (value < MIN_TRANSLATE * 3) {
            value = MIN_TRANSLATE * 3;
        }
        return value;
    }

    private float getRateProgressGradientWidth() {
        float value = getRateProgressWidth() * 0.3f;
        value = value < MIN_GRADIENTWIDTH ? MIN_GRADIENTWIDTH : value;
        return value;
    }

    private float getProgressWidth() {
        return mViewWidth * mRate;
    }

    private float getRateProgressWidth(float rate) {
        return mViewWidth * rate;
    }

    private float getMaxTranslate() {
        return getProgressWidth() + getProgressGradientWidth();
    }

    private float getRateMaxTranslate() {
        return getRateProgressGradientWidth() + getRateProgressWidth();
    }

    private float getTranslateDx() {
        return getMaxTranslate() * 0.01f;
    }


    private float getRateTranslateDx() {
        float dx = getRateProgressWidth() * 0.04f;
        if (dx > MAX_TRANSLATE) {
            dx = MAX_TRANSLATE;
        } else if (dx > MIDDLE_TRANSLATE) {
            dx = MIDDLE_TRANSLATE;
        } else if (dx > LOW_TRANSLATE) {
            dx = LOW_TRANSLATE;
        }
        return dx;
    }

    private void addProgressData() {
        if (canUpdateProgressData && !isPause) {
            ArrayList<Float> al = ProgressCache.read(mKey);
            int len = al.size();
            if (mProgressData.size() == len) {
                if (len > 0&& mProgressData.get(len - 1).floatValue() == mRate) {
                    al.remove(len - 1);
                } else {
                    al.add(mRate);
                }
            }
            mProgressData = al;
        }
    }

    /**
     * 不分异步跟同步
     */
    private void hideGradientAnimation() {
        isEffectAnimated = false;
        removeCallbacks(mGradientAnimation);
        resetGradientMatrix();
        postInvalidate();
    }

    private float lastMaxTranslate;
    private Runnable mGradientAnimation = new Runnable() {
        @Override
        public void run() {
            long interval=System.currentTimeMillis()-mLastTime;
            if (interval < 16) {
                postDelayed(this, 16-interval);
                mLastTime = System.currentTimeMillis();
                return;
            }
            float dx = getRateTranslateDx();
            mTranslateX += dx;
            float maxtranslate = 0;
            if (mTranslateX > lastMaxTranslate) {
                maxtranslate = lastMaxTranslate;
            } else {
                maxtranslate = getRateMaxTranslate();
            }

            if (mTranslateX >= maxtranslate) {
                mTranslateX = maxtranslate;
            }
            updateGradientMatrix();
            if (mTranslateX == maxtranslate) {
                mTranslateX = 0;
                renewProgressGradient();
            }
            lastMaxTranslate = getRateMaxTranslate();
            invalidate();
            postDelayed(this, 16);
            if (!isGradientAnimated()) {
                //某些场景下会没有终止
                hideGradientAnimation();
            }
            mLastTime = System.currentTimeMillis();
        }
    };

    private void resetGradientMatrix() {
        mTranslateX = 0;
        checkAnimationParams();
        mGradientMatrix.setTranslate(getEffectLeft(), 0);
        mProgressGradient.setLocalMatrix(mGradientMatrix);
        mEffectPaint.setShader(mProgressGradient);
    }

    private void checkAnimationParams() {
        if (mProgressGradient == null) {
            mProgressGradient = new LinearGradient(-getRateProgressGradientWidth(), 0, 0, 0, new int[]{mEffectProgressColor, Color.parseColor("#ffa816"), Color.parseColor("#febd1e"), Color.parseColor("#ffdb0c"), Color.parseColor("#ffef2f"), mEffectProgressColor}, new float[]{0, 0.01f, 0.33f, 0.66f, 0.99f, 1f}, Shader.TileMode.CLAMP);
        }
        if (mGradientMatrix == null) {
            mGradientMatrix = new Matrix();
        }
    }

    private void updateGradientMatrix() {
        checkAnimationParams();
        mGradientMatrix.setTranslate(mTranslateX + getEffectLeft(), 0);
        mProgressGradient.setLocalMatrix(mGradientMatrix);
    }


    private void afterUpdateProgress() {
        if (mRate == 1) {
            isPause = false;
            if (isEffectAnimated) {
                stopGradientAnimation();
            }
            canUpdateProgressData = false;
            clearProgressData();
        }
    }

    private void clearProgressData() {
        mProgressData = new ArrayList<>();
        ProgressCache.clear(mKey);
    }

    private void setProgress(int progress) {
        float rate = progress * 1.0f / mProgressMax;
        needUpdateGradient = mRate != rate;
        mRate = rate;
        if (mProgressData.size() > 0 && mRate < mProgressData.get(mProgressData.size() - 1)) {
            clearProgressData();
        }
    }

    private void renewProgressGradient() {
        if (needUpdateGradient) {
            mProgressGradient = new LinearGradient(-getRateProgressGradientWidth(), 0, 0, 0, new int[]{mEffectProgressColor, Color.parseColor("#ffa816"), Color.parseColor("#febd1e"), Color.parseColor("#ffdb0c"), Color.parseColor("#ffef2f"), mEffectProgressColor}, new float[]{0, 0.01f, 0.33f, 0.66f, 0.99f, 1f}, Shader.TileMode.CLAMP);
            resetGradientMatrix();
            needUpdateGradient = false;
        }
    }

    private boolean lastStateIsUnGradiented() {
        return mProgressData.size() % 2 == 0;
    }

    @Override
    public void startGradientAnimation() {
        if (isPause) {
            return;
        }
        if (!isEffectAnimated && mRate < 1) {
            readPoints();
            if (lastStateIsUnGradiented()) {
                addProgressData();
            }
            renewProgressGradient();
            post(mGradientAnimation);
            isEffectAnimated = true;
        }
    }

    @Override
    public void stopGradientAnimation() {
        if (isPause) {
            return;
        }
        if (isEffectAnimated) {
            readPoints();
            hideGradientAnimation();
            if (!lastStateIsUnGradiented()) {
                addProgressData();
            }

        }
    }

    private void clearGradienAnimation() {
        if (isEffectAnimated) {
            hideGradientAnimation();
        }
    }

    @Override
    public void pause() {
        if (mRate < 1) {
            isPause = true;
            if (isEffectAnimated) {
                hideGradientAnimation();
            }
            invalidate();
        }
    }

    @Override
    public void resume() {
        isPause = false;
        if (lastStateIsUnGradiented()) {
            invalidate();
        } else {
            if (!isEffectAnimated && mRate < 1) {
                renewProgressGradient();
                post(mGradientAnimation);
                isEffectAnimated = true;
            }
        }

    }

    @Override
    public void syncProgress(int progress) {
        if (!checkProgressValid(progress)) {
            return;
        }
        setProgress(progress);
        invalidate();
        afterUpdateProgress();
    }

    @Override
    public void asyncProgress(final int progress) {
        if (!checkProgressValid(progress)) {
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                setProgress(progress);
                invalidate();
                afterUpdateProgress();
            }
        });

    }

    @Override
    public void setMax(int max) {
        this.mProgressMax = max;
    }

    @Override
    public boolean isGradientAnimated() {
        return isEffectAnimated && canUpdateProgressData;
    }

    @Override
    public boolean isPause() {
        return isPause;
    }

    @Override
    public void hideProgresss() {
        stopGradientAnimation();
        this.setVisibility(View.GONE);
    }

    @Override
    public void showProgress() {
        this.setVisibility(View.VISIBLE);
    }

    @Override
    public void setSaveKey(String key) {
        if (TextUtils.equals(key, mKey)) {
            return;
        }
        this.mKey = key;
        isEffectAnimated=false;
        mRate=0;
        mProgressData=ProgressCache.read(mKey);
    }

    @Override
    public String getSaveKey() {
        return mKey;
    }

    @Override
    public ProgressControl getProgressControl() {
        return this;
    }

    private String progressPointToString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (mProgressData != null) {
            for (int i = 0, len = mProgressData.size(); i < len; i++) {
                stringBuilder.append(mProgressData.get(i));
                stringBuilder.append(";");
            }
        }
        return stringBuilder.toString();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCacheCanvas.drawColor(Color.WHITE);
        mCacheCanvas.drawRect(0f, mProgressTop, mViewWidth, mProgressTop + mProgressHeight, mBackgroundPaint);
        if (isPause) {
            mCacheCanvas.drawRect(0f, mProgressTop, getProgressWidth(), mProgressTop + mProgressHeight, mPausePaint);
            canvas.drawBitmap(mCacheBitmap, 0, 0, null);
            return;
        }
        mProgressData=ProgressCache.read(mKey);
        if (mProgressData.size() > 0) {

            {
                //没有进度记录点有进度
                float right = mProgressData.get(0);
                if (right > 0) {
                    mCacheCanvas.drawRect(0, mProgressTop, getRateProgressWidth(mProgressData.get(0)), mProgressTop + mProgressHeight, mCommonProgressPaint);
                }
            }
            //黄色片段
            for (int i = 0, len = mProgressData.size() - 1; i < len; i += 2) {
                float left = getRateProgressWidth(mProgressData.get(i));
                float right = getRateProgressWidth(mProgressData.get(i + 1));
                if (left < right) {
                    mCacheCanvas.drawRect(left, mProgressTop, right, mProgressTop + mProgressHeight, mEffectProgressPaint);
                }
            }
            //蓝色片段
            for (int i = 1, len = mProgressData.size() - 1; i < len; i += 2) {
                float left = getRateProgressWidth(mProgressData.get(i));
                float right = getRateProgressWidth(mProgressData.get(i + 1));
                if (left < right) {
                    mCacheCanvas.drawRect(left, mProgressTop, right, mProgressTop + mProgressHeight, mCommonProgressPaint);
                }
            }
            Log.i("progress", "progressdata=" + progressPointToString() + ";saveKey=" + getSaveKey() + ";progresscache=" + ProgressCache.printProgressData(mKey));
        } else {
            //没有进度记录点有进度
            mCacheCanvas.drawRect(0, mProgressTop, getRateProgressWidth(mRate), mProgressTop + mProgressHeight, mCommonProgressPaint);
        }

        //蓝色前进效果
        if (!isGradientAnimated() && mProgressData.size() > 0) {
            //没有加速的进度条前进或者进度已经走完一遍
            float rate = mProgressData.get(mProgressData.size() - 1);
            if (rate < 1) {
                float left = getRateProgressWidth(rate);
                float right = getProgressWidth();
                if (left < right) {
                    mCacheCanvas.drawRect(left, mProgressTop, right, mProgressTop + mProgressHeight, mCommonProgressPaint);
                }
            }
        }
        //动画效果
        if (isGradientAnimated() && mProgressData.size() > 0) {
            //光带特效
            float left = getEffectLeft();
            mCacheCanvas.drawRect(left, mProgressTop, getProgressWidth(), mProgressTop + mProgressHeight, mEffectPaint);
            //火箭头
            float bitmapLeft=0;
            if(mRate==0){
                bitmapLeft=mProgressData.get(mProgressData.size()-1);
            }else{
                bitmapLeft = getRateProgressWidth(mRate);
            }
            bitmapLeft -= mRocketTailerWidth;
            if (bitmapLeft + mRocketBitmap.getWidth() < mViewWidth) {
                mCacheCanvas.drawBitmap(mRocketBitmap, bitmapLeft, 0, null);
            }

        }
        canvas.drawBitmap(mCacheBitmap, 0, 0, null);
    }
}
