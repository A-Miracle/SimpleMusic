package com.ctao.music.ui.common;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.ctao.baselib.utils.BarUtils;
import com.ctao.baselib.utils.ResourcesUtils;
import com.ctao.music.R;
import com.ctao.music.ui.base.MvpActivity;

/**
 * Created by A Miracle on 2017/5/18.
 * 调色板
 */
public final class ColorChooserActivity extends MvpActivity implements ColorChooserDialog.ColorCallback{

    public static final String VALUE_COLOR = "VALUE_COLOR";

    @Override
    protected int getLayoutId() { return 0; }

    @Override
    protected void initTheme() {
        isNeedTheme = false;
    }

    @Override
    protected void initThemeComplete() {}

    @Override
    protected void initSkin() {}

    @Override
    protected void immersiveStatuNavBar() {
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT);
    }

    @Override
    protected void onAfterSetContentLayout(Bundle savedInstanceState) {
        int preselectColor = -1;
        Intent intent = getIntent();
        if(intent != null){
            preselectColor = intent.getIntExtra(VALUE_COLOR, preselectColor);
        }

        // this 需满足 extends AppCompatActivity & ColorCallback
        new ColorChooserDialog.Builder(this, R.string.color_palette) //标题
                .theme(Theme.LIGHT)
                .accentMode(false) // 是否整齐排列
                .titleSub(R.string.color_sub) // 子标题
                .doneButton(R.string.color_done)
                .cancelButton(R.string.color_cancel)
                .backButton(R.string.color_back)
                .customButton(R.string.color_custom)
                .presetsButton(R.string.color_presets)
                .preselect(preselectColor != -1 ? preselectColor : ResourcesUtils.getValueOfColorAttr(this, -1, R.attr.colorPrimary))  // optionally preselects a color
                .dynamicButtonColor(true) // button是否根据颜色变换
                .show(this);
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        Intent data = new Intent();
        data.putExtra(VALUE_COLOR, selectedColor);
        setResult(RESULT_OK, data);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
        finish();
    }
}
