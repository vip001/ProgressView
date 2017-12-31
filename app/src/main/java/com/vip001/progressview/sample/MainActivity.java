package com.vip001.progressview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.vip001.progressview.view.FlickerSectionProgressView;
import com.vip001.progressview.view.FlickerSingleProgressView;
import com.xunlei.progressview.R;

public class MainActivity extends Activity {

    private FlickerSingleProgressView mSingleView;
    private FlickerSectionProgressView mSectionView;
    private Button mSingleEffectControlBtn;
    private Button mSingleProgressControlBtn;
    private Button mSectionEffectControlBtn;
    private Button mSectionProgressControlBtn;
    private int mSectionProgress= 0;
    private int mSingleProgress=0;
    private static final int TYPE_COUNTDOWN_SINGLE=1;
    private static final int TYPE_COUNTDOWN_SECTION=2;
    private Handler mCountDownHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == TYPE_COUNTDOWN_SINGLE) {
               if(mSingleProgress==100){
                   return;
               }
                mSingleProgress += 1;
                mSingleView.syncProgress(mSingleProgress);
                mCountDownHandler.sendEmptyMessageDelayed(TYPE_COUNTDOWN_SINGLE, 1000);
            }else if(msg.what==TYPE_COUNTDOWN_SECTION){
               if(mSectionProgress==100){
                   return;
               }
                mSectionProgress += 1;
                mSectionView.syncProgress(mSectionProgress);
                mCountDownHandler.sendEmptyMessageDelayed(TYPE_COUNTDOWN_SECTION, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        mSingleView = (FlickerSingleProgressView) this.findViewById(R.id.singleView);
        mSectionView = (FlickerSectionProgressView) this.findViewById(R.id.sectionView);
        mSingleEffectControlBtn = (Button) this.findViewById(R.id.singleEffectControl);
        mSingleProgressControlBtn = (Button) this.findViewById(R.id.singleProgressControl);
        mSectionEffectControlBtn = (Button) this.findViewById(R.id.sectionEffectControl);
        mSectionProgressControlBtn = (Button) this.findViewById(R.id.sectionProgressControl);
        mSingleEffectControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mSingleView.isPause()){
                    if(mSingleView.isGradientAnimated()){
                        mSingleView.stopGradientAnimation();
                    }else{
                        mSingleView.startGradientAnimation();
                    }
                }
                updateSingleButtonText();

            }
        });
        mSingleProgressControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSingleView.isPause()){
                    mSingleView.resume();
                    mCountDownHandler.sendEmptyMessage(TYPE_COUNTDOWN_SINGLE);
                }else{
                    mSingleView.pause();
                    mCountDownHandler.removeMessages(TYPE_COUNTDOWN_SINGLE);
                }
                updateSingleButtonText();
            }
        });
        mSectionProgressControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSectionView.isPause()){
                    mSectionView.resume();
                    mCountDownHandler.sendEmptyMessage(TYPE_COUNTDOWN_SECTION);
                }else{
                    mSectionView.pause();
                    mCountDownHandler.removeMessages(TYPE_COUNTDOWN_SECTION);
                }
                updateSectionButtonText();
            }
        });
        mSectionEffectControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mSectionView.isPause()){
                    if(mSectionView.isGradientAnimated()){
                        mSectionView.stopGradientAnimation();
                    }else{
                        mSectionView.startGradientAnimation();
                    }
                }

                updateSectionButtonText();
            }
        });
        mCountDownHandler.sendEmptyMessage(TYPE_COUNTDOWN_SINGLE);
        mCountDownHandler.sendEmptyMessage(TYPE_COUNTDOWN_SECTION);
        updateSingleButtonText();
        updateSectionButtonText();
    }

    private void updateSectionButtonText() {
        if (mSectionView.isPause()) {
            mSectionProgressControlBtn.setText("恢复");
        } else {
            mSectionProgressControlBtn.setText("暂停");
        }
        if(mSectionView.isGradientAnimated()){
            mSectionEffectControlBtn.setText("停止加速");
        }else{
            mSectionEffectControlBtn.setText("加速");
        }
    }
    private void updateSingleButtonText() {
        if (mSingleView.isPause()) {
            mSingleProgressControlBtn.setText("恢复");
        } else {
            mSingleProgressControlBtn.setText("暂停");
        }
        if(mSingleView.isGradientAnimated()){
            mSingleEffectControlBtn.setText("停止加速");
        }else{
            mSingleEffectControlBtn.setText("加速");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCountDownHandler.removeMessages(TYPE_COUNTDOWN_SINGLE);
        mCountDownHandler.removeMessages(TYPE_COUNTDOWN_SECTION);
    }
}
