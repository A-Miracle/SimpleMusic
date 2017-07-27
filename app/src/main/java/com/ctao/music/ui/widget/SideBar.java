package com.ctao.music.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.ctao.music.R;

/**
 * Created by A Miracle on 2017/6/25.
 */
public final class SideBar extends View {
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    public static String[] b = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    private int choose = -1;
    private Paint paint = new Paint();
    private TextView mTextDialog;

    private int defColor;
    private int pressedColor;

    public void setChoose(String letter) {
        int index = 0;
        for (String s : b) {
            if(s.equals(letter)){
                choose = index;
                postInvalidate();
                return;
            }
            index++;
        }
    }

    public void setTextView(TextView textDialog) {
        mTextDialog = textDialog;
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SideBar(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        defColor = context.getResources().getColor(R.color.def_bar_title_color);
        pressedColor=  context.getResources().getColor(R.color.white);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        int singleHeight = height / b.length;
        for (int i = 0; i < b.length; i++) {
            // paint.setColor(Color.rgb(33, 65, 98));
            paint.setColor(defColor);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            paint.setTextSize(20);

            float measureValue = paint.measureText(b[i]);
            float xPos = width / 2 - measureValue / 2;
            float yPos = singleHeight * i + singleHeight;

            if (i == choose) {
                canvas.drawRect(measureValue / 2,
                        yPos - singleHeight,
                        getWidth() - measureValue / 2,
                        yPos, paint);

                paint.setColor(pressedColor);
                paint.setFakeBoldText(true);
            }

            canvas.drawText(b[i], xPos, yPos - (singleHeight - measureValue) / 2, paint);
            paint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int) (y / getHeight() * b.length);
        switch (action) {
            case MotionEvent.ACTION_UP:
                //choose = -1;//
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.GONE);
                }
                break;
            default:
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        if (listener != null) {
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        if (mTextDialog != null) {
                            mTextDialog.setText(b[c]);
                            mTextDialog.setVisibility(View.VISIBLE);
                        }
                        choose = c;
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener listener) {
        onTouchingLetterChangedListener = listener;
    }

    public interface OnTouchingLetterChangedListener {
        void onTouchingLetterChanged(String s);
    }
}
