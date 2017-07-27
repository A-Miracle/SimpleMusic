package com.ctao.music.module.imageselect;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.ctao.baselib.utils.BarUtils;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.ui.base.MvpActivity;
import com.ctao.music.utils.ATEUtils;
import com.ctao.music.utils.UIUtils;
import com.yalantis.ucrop.UCropActivity;

/**
 * Created by A Miracle on 2016/10/29.
 */
public class CropActivity extends UCropActivity {
    private int themeColor; // 主题颜色

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(ATEUtils.getActivityTheme());
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent != null){
            themeColor = intent.getIntExtra(MvpActivity.TYPE_THEME_COLOR, themeColor);
        }

        // 皮肤
        initSkin();

        //沉浸式状态栏
        immersiveStatuBar();
    }

    private void initSkin() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if(SPUtils.getBoolean(Constant.SP_SKIN_IS_ON, false)){
            UIUtils.setAlpha(SPUtils.getInt(Constant.SP_SKIN_ALPHA, 220));
            UIUtils.setupSkinBlurry(getContentView());
        }
    }

    public View getContentView(){
        return findViewById(Window.ID_ANDROID_CONTENT);
    }

    private void immersiveStatuBar() {
        // 状态栏
        BarUtils.setStatusBarColor(this, themeColor);
        // 导航栏
        BarUtils.setNavigationBarColor(this, Color.BLACK);
    }
}
