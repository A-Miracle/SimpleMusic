package com.ctao.music.ui.common;

import android.view.View;

import com.ctao.music.ui.base.MvpFragment;

/**
 * Created by A Miracle on 2017/7/24.
 */
public abstract class CommonFragment extends MvpFragment {
    public abstract String getTitle();
    public void initMenu(View contentView) {}
    public void initOtherOnCreateInLast(View contentView) {}
    public void onFinish() {}


}
