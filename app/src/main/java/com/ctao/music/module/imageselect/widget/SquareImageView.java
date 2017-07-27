package com.ctao.music.module.imageselect.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by A Miracle on 2016/10/28.
 */
public class SquareImageView extends android.support.v7.widget.AppCompatImageView {
    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (MeasureSpec.EXACTLY == widthMode) {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
        } else if (MeasureSpec.EXACTLY == heightMode) {
            setMeasuredDimension(getMeasuredHeight(), getMeasuredHeight());
        }
    }
}
