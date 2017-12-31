package com.vip001.progressview.sample;

/**
 * Created by vip001 on 2017/12/29.
 */

public class BaseState {
    protected int mState;

    public void setState(int state) {
        if(checkState(state)){
            mState = mState & ~state | state;
        }

    }

    public boolean hasState(int state) {
        return (mState & state) == state;
    }

    public void clearState(int state) {
        if(checkState(state)){
            mState = mState & ~state;
        }

    }

    protected boolean checkState(int state) {
        return state%2==0;
    }
}
