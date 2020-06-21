package com.ctao.music.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by A Miracle on 2017/6/26.
 * 自定义字体图标
 */
public final class IconTextView extends AppCompatTextView {
    public IconTextView(Context context) {
        this(context, null);
    }

    public IconTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public IconTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //设置字体图标
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/iconfont.ttf");
        setTypeface(font);
    }
}
