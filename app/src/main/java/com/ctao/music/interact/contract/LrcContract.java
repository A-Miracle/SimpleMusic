package com.ctao.music.interact.contract;

import com.ctao.music.interact.IBasePresenter;
import com.ctao.music.interact.IBaseView;

import java.io.File;

/**
 * Created by A Miracle on 2017/7/19.
 * View 与 Presenter 之间的 Contract
 */
public interface LrcContract {
    interface View extends IBaseView<Presenter> {
        void showLyric(File file);
    }

    interface Presenter extends IBasePresenter {
        void downloadLrcFile(String title, String artist, long duration);
    }
}
