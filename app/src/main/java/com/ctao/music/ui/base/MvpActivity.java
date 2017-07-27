package com.ctao.music.ui.base;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.ctao.baselib.ui.BaseActivity;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.interact.view.ILoadingView;
import com.ctao.music.ui.MainActivity;
import com.ctao.music.utils.ATEUtils;
import com.ctao.music.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by A Miracle on 2017/6/24.
 */
public abstract class MvpActivity extends BaseActivity implements ILoadingView, ATEActivityThemeCustomizer {
    private Unbinder unbinder;

    @Override
    protected void bindButterKnife() {
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void unbindButterKnife() {
        unbinder.unbind();
    }

    @Override
    protected void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    @Override
    protected void unregisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initData() {
        super.initData();
        themeColor = Config.primaryColor(this, getATEKey());
    }

    @Override
    protected void initSkin() {
        if(!SPUtils.getBoolean(Constant.SP_SKIN_IS_ON, false)){
            return;
        }
        UIUtils.setAlpha(SPUtils.getInt(Constant.SP_SKIN_ALPHA, 220));
        setSkin();
    }

    private void setSkin() {
        View contentView = getContentView();
        if (this instanceof MainActivity) {
            UIUtils.setupSkin(contentView);
        } else {
            UIUtils.setupSkinBlurry(contentView);
        }
    }

    @Override
    public void showProgress() { }

    @Override
    public void hideProgress() { }

    //https://github.com/greenrobot/EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()){
            case MessageEvent.MUSIC_UPDATE_SKIN: // 变更壁纸, 异步生成模糊并通知
                if(this instanceof MainActivity){
                    UIUtils.setupSkin(getContentView(), false);
                }
                break;
            case MessageEvent.MUSIC_UPDATE_SKIN_BLURRY: // 模糊壁纸
                if(!(this instanceof MainActivity)){
                    UIUtils.setupSkinBlurry(getContentView());
                }
                break;
            case MessageEvent.MUSIC_UPDATE_SKIN_ALPHA: // 壁纸透明度
                setSkin(); // 可以使用缓存的
                break;
            case MessageEvent.MUSIC_UPDATE_SKIN_ON_OFF: // 壁纸开关
                if((boolean) event.getParam()){
                    setSkin(); // 可以使用缓存的
                }else{
                    getContentView().setBackground(null);
                }
                break;
        }
    }

    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)
                ? R.style.AppThemeDark : R.style.AppTheme;
    }

    @Nullable
    @Override
    public String getATEKey() {
        return ATEUtils.getATEKey();
    }

    @Override
    public void recreate() {
        startActivity(new Intent(this, getClass())); // 开一个新的自己
        finish(); // 关掉旧的自己
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
