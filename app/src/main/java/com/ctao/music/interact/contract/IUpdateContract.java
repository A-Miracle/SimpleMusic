package com.ctao.music.interact.contract;


import com.ctao.music.interact.IBasePresenter;
import com.ctao.music.interact.IBaseView;
import com.ctao.music.interact.model.Update;
import com.ctao.music.interact.view.ILoadingView;

import java.io.File;

/**
 * View 与 Presenter 之间的 Contract
 */
public interface IUpdateContract {
    interface View extends IBaseView<Presenter>, ILoadingView {
        void checkUpdate(Update update);
        void downloadComplete(File file);
        void downloadProgress(int progress);
    }

    interface Presenter extends IBasePresenter {
        void checkUpdate();
        void downloadApk(String fileName);
    }
}
