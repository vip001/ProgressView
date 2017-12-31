package com.vip001.progressview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import com.vip001.progressview.utils.UIUtils;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by vip001 on 2017/9/7.
 */

public class FlickerSingleProgressView extends View implements ProgressControl {
    public Paint mBackgroundPaint = null;
    private Paint mEffectPaint = null;
    private Paint mEffectProgressPaint = null;
    private Paint mPausePaint;
    private Matrix mGradientMatrix;
    Bitmap mCacheBitmap = null;
    Canvas mCacheCanvas = null;
    private float mViewWidth;
    private LinearGradient mProgressGradient;
    private boolean needUpdateGradient = false;
    private boolean canUpdateProgressData;
    private float mTranslateX;
    private int mProgressMax = 100;
    private int mEffectProgressColor = Color.parseColor("#1294f6");
    private float mRate = 0;
    private boolean isEffectAnimated;
    private int mProgressTop;
    private int mProgressHeight;
    private int mMultipleParams = 1;
    private boolean isPause;
    private String mKey;
    private ArrayList<Float> mProgressData;
    private static final float MIN_GRADIENTWIDTH;
    private static final float MAX_TRANSLATE;
    private static final float MIDDLE_TRANSLATE;
    private static final float LOW_TRANSLATE;
    private static final float MIN_TRANSLATE;
    static {
        MIN_TRANSLATE = UIUtils.dip2fpx( 1);
        MAX_TRANSLATE = UIUtils.dip2fpx(3f);
        MIDDLE_TRANSLATE = UIUtils.dip2fpx( 2.5f);
        LOW_TRANSLATE = UIUtils.dip2fpx( 2f);
        MIN_GRADIENTWIDTH = UIUtils.dip2fpx( 4f);
    }

    public FlickerSingleProgressView(Context context) {
        super(context);
        initView();
    }


    public FlickerSingleProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FlickerSingleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(21)
    public FlickerSingleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        readPoints();
    }

    private boolean lastStateIsUnGradiented() {
        return mProgressData.size() % 2 == 0;
    }

    private void addProgressData() {
        if (canUpdateProgressData && !isPause) {
            ArrayList<Float> al = ProgressCache.read(mKey);
            int len = al.size();
            if (mProgressData.size() == len) {
                if (len > 0 && mProgressData.get(len - 1).floatValue() == mRate) {
                    al.remove(len - 1);
                } else {
                    al.add(mRate);
                }
            }
            mProgressData = al;
        }
    }

    private void readPoints() {
        ArrayList<Float> data = ProgressCache.read(mKey);
        if (data.size() != mProgressData.size()) {
            mProgressData = data;
        }
    }

    private void initView() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.parseColor("#efeff0"));
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setAntiAlias(true);
        mEffectPaint = new Paint();
        mEffectPaint.setStyle(Paint.Style.FILL);
        mEffectPaint.setAntiAlias(true);
        mEffectProgressPaint = new Paint();
        mEffectProgressPaint.setColor(mEffectProgressColor);
        mEffectProgressPaint.setStyle(Paint.Style.FILL);
        mEffectProgressPaint.setAntiAlias(true);
        canUpdateProgressData = true;
        mGradientMatrix = new Matrix();

        mProgressTop = UIUtils.dip2ipx( 0 * mMultipleParams);
        mProgressHeight =UIUtils.dip2ipx( 2 * mMultipleParams);
        mPausePaint = new Paint();
        mPausePaint.setAntiAlias(true);
        mPausePaint.setColor(Color.parseColor("#cfd0d5"));
        isPause = false;
        mProgressData = new ArrayList<Float>();
        mKey= UUID.randomUUID().toString();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mCacheBitmap == null || mCacheBitmap.getWidth() != w || mCacheBitmap.getHeight() != h) {
            mViewWidth =w>0?w:1;
            mCacheBitmap = Bitmap.createBitmap(w>0?w:1,h>0?h:1, Bitmap.Config.ARGB_8888);
            mCacheCanvas = new Canvas();
            mCacheCanvas.setBitmap(mCacheBitmap);
            needUpdateGradient = true;
            renewProgressGradient();
        }
        //startGradientAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // clearGradienAnimation();
    }

    private float getProgressGradientWidth() {
        float value = getProgressWidth() * 0.3f;
        value = value < MIN_GRADIENTWIDTH ? MIN_GRADIENTWIDTH : value;
        return value;
    }


    private float getProgressWidth() {
        return mViewWidth * mRate;
    }


    private float getMaxTranslate() {
        return getProgressWidth() + getProgressGradientWidth();
    }


    private float getTranslateDx() {

        float dx = getProgressWidth() * 0.04f;
        if (dx > MAX_TRANSLATE && mRate > 0.6f) {
            dx = MAX_TRANSLATE;
        } else if (dx > MIDDLE_TRANSLATE && mRate > 0.5f) {
            dx = MIDDLE_TRANSLATE;
        } else if (dx > LOW_TRANSLATE && mRate > 0.4f) {
            dx = LOW_TRANSLATE;
        } else if (dx > MIN_TRANSLATE) {
            dx = MIN_TRANSLATE;
        }
        return dx;
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
    private long mLastTime;
    private Runnable mGradientAnimation = new Runnable() {
        @Override
        public void run() {
            long interval=System.currentTimeMillis()-mLastTime;
            if (interval < 16) {
                postDelayed(this, 16-interval);
                mLastTime = System.currentTimeMillis();
                return;
            }
            float dx = getTranslateDx();
            mTranslateX += dx;
            float maxtranslate = 0;
            if (mTranslateX > lastMaxTranslate) {
                maxtranslate = lastMaxTranslate;
            } else {
                maxtranslate = getMaxTranslate();
            }
            if (mTranslateX >= maxtranslate) {
                mTranslateX = maxtranslate;
            }
            updateGradientMatrix();
            if (mTranslateX == maxtranslate) {
                mTranslateX = 0;
                renewProgressGradient();
            }
            lastMaxTranslate = getMaxTranslate();
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
        checkAnimationParams();
        mGradientMatrix.setTranslate(0, 0);
        mProgressGradient.setLocalMatrix(mGradientMatrix);
        mEffectPaint.setShader(mProgressGradient);
    }

    private void checkAnimationParams() {
        if (mGradientMatrix == null) {
            mGradientMatrix = new Matrix();
        }
        if (mProgressGradient == null) {
            mProgressGradient = new LinearGradient(-getProgressGradientWidth(), 0, 0, 0, new int[]{mEffectProgressColor, Color.parseColor("#1898f6"), Color.parseColor("#45b9f9"), Color.parseColor("#60ccfa"), Color.parseColor("#88e8fd"), mEffectProgressColor}, new float[]{0, 0.01f, 0.33f, 0.66f, 0.99f, 1f}, Shader.TileMode.CLAMP);
        }
    }

    private void updateGradientMatrix() {
        checkAnimationParams();
        mGradientMatrix.setTranslate(mTranslateX, 0);
        mProgressGradient.setLocalMatrix(mGradientMatrix);
    }

    private void afterUpdateProgress() {
        if (mRate == 1) {
            if (isEffectAnimated) {
                stopGradientAnimation();
            }
            clearProgressData();
            canUpdateProgressData = false;
        }
    }

    private void setProgress(int progress) {
        float rate = progress * 1.0f / mProgressMax;
        needUpdateGradient = mRate != rate;
        mRate = rate;
        mProgressData=ProgressCache.read(mKey);
        if (mProgressData.size() > 0 && mRate < mProgressData.get(mProgressData.size() - 1)) {
            clearProgressData();
        }
    }

    private void clearProgressData() {
        mProgressData = new ArrayList<>();
        ProgressCache.clear(mKey);
    }

    private void renewProgressGradient() {
        if (needUpdateGradient) {
            mProgressGradient = new LinearGradient(-getProgressGradientWidth(), 0, 0, 0, new int[]{mEffectProgressColor, Color.parseColor("#1898f6"), Color.parseColor("#45b9f9"), Color.parseColor("#60ccfa"), Color.parseColor("#88e8fd"), mEffectProgressColor}, new float[]{0, 0.01f, 0.33f, 0.66f, 0.99f, 1f}, Shader.TileMode.CLAMP);
            resetGradientMatrix();
            needUpdateGradient = false;
        }
    }

    private boolean checkProgressValid(int progress) {
        return progress >= 0 && progress <= mProgressMax;
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
        mRate = 0;
        isEffectAnimated = false;
        mProgressData = ProgressCache.read(mKey);
    }

    @Override
    public String getSaveKey() {
        return mKey;
    }

    @Override
    public ProgressControl getProgressControl() {
        return this;
    }

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

    private void clearGradienAnimation() {
        if (isEffectAnimated) {
            hideGradientAnimation();
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
        //动画效果
        mCacheCanvas.drawRect(0f, mProgressTop, getProgressWidth(), mProgressTop + mProgressHeight, mEffectPaint);

        canvas.drawBitmap(mCacheBitmap, 0, 0, null);
    }
}
