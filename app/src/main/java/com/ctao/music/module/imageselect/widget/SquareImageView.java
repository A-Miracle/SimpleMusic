package com.ctao.music.module.imageselect.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by A Miracle on 2016/10/28.
 */
public class SquareImageView extends AppCompatImageView {
    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (MeasureSpec.EXACTLY == widthMode) {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
        } else if (MeasureSpec.EXACTLY == heightMode) {
            setMeasuredDimension(getMeasuredHeight(), getMeasuredHeight());
        }
    }
}
