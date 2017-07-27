package com.ctao.music.ui.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by A Miracle on 2017/6/26.
 * 自定义字体图标
 */
public final class IconTextView extends android.support.v7.widget.AppCompatTextView{
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
