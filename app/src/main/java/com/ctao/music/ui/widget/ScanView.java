package com.ctao.music.ui.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.appthemeengine.Config;
import com.ctao.baselib.utils.DisplayUtils;
import com.ctao.music.R;
import com.ctao.music.utils.ATEUtils;

public class ScanView extends View {
    public final static int CLOCKWISE = 1;
    public final static int COUNTERCLOCKWISE = -1;

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;

    private int radarRadius;
    private int centerX, centerY;
    private int circleColor, radarColor;
    private int defaultWidth, defaultHeight;

    private Paint mPaintCircle;
    private Paint mPaintRadar;

    private int mDegree = -90; //角度
    private int mRate, mTempRate = 2; //速率
    private int mDirection = CLOCKWISE; //方向

    public ScanView(Context context) {
        this(context, null);
    }

    public ScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        radarRadius = Math.min(w, h);
    }

    private void init(Context context, AttributeSet attrs) {
        String ateKey = ATEUtils.getATEKey();
        int primaryColor = Config.primaryColor(getContext(), ateKey);
        if(primaryColor != Color.TRANSPARENT){ // 只要不是全透明, 取RGB值
            int primaryColorDark = Config.primaryColorDark(getContext(), ateKey);
            radarColor = Color.argb(255, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor));
            circleColor = Color.argb(255, Color.red(primaryColorDark), Color.green(primaryColorDark), Color.blue(primaryColorDark));
        }else{
            if(ATEUtils.getActivityTheme() == R.style.AppTheme){
                radarColor = getResources().getColor(R.color.colorPrimary);
                circleColor = getResources().getColor(R.color.colorPrimaryDark);
            }else{
                radarColor = getResources().getColor(R.color.colorPrimary_Dark);
                circleColor = getResources().getColor(R.color.colorPrimaryDark_Dark);
            }
        }
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScanView);
            circleColor = ta.getColor(R.styleable.ScanView_circleColor, circleColor);
            radarColor = ta.getColor(R.styleable.ScanView_radarColor, radarColor);
            mDegree = ta.getInteger(R.styleable.ScanView_degree, mDegree);
            mTempRate = ta.getInteger(R.styleable.ScanView_rate, mTempRate);
            mDirection = ta.getInteger(R.styleable.ScanView_direction, CLOCKWISE);
            ta.recycle();
        }

        // 初始化画笔
        mPaintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        mPaintCircle.setColor(circleColor);
        mPaintCircle.setStrokeWidth(2);

        mPaintRadar = new Paint(Paint.ANTI_ALIAS_FLAG);

        // 得到当前屏幕的像素宽高
        defaultWidth = DisplayUtils.converDip2px(DEFAULT_WIDTH);
        defaultHeight = DisplayUtils.converDip2px(DEFAULT_HEIGHT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resultWidth;
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);

        if (modeWidth == MeasureSpec.EXACTLY) {
            resultWidth = sizeWidth;
        } else {
            resultWidth = defaultWidth;
            if (modeWidth == MeasureSpec.AT_MOST) {
                resultWidth = Math.min(resultWidth, sizeWidth);
            }
        }

        int resultHeight;
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (modeHeight == MeasureSpec.EXACTLY) {
            resultHeight = sizeHeight;
        } else {
            resultHeight = defaultHeight;
            if (modeHeight == MeasureSpec.AT_MOST) {
                resultHeight = Math.min(resultHeight, sizeHeight);
            }
        }

        setMeasuredDimension(resultWidth, resultHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 分别绘制四个圆
        canvas.drawCircle(centerX, centerY, radarRadius / 7, mPaintCircle);
        canvas.drawCircle(centerX, centerY, radarRadius / 4, mPaintCircle);
        canvas.drawCircle(centerX, centerY, radarRadius / 3, mPaintCircle);
        canvas.drawCircle(centerX, centerY, 3 * radarRadius / 7, mPaintCircle);

        // 设置颜色渐变从透明到不透明
        Shader shader = new SweepGradient(centerX, centerY, Color.TRANSPARENT, radarColor);
        mPaintRadar.setShader(shader);

        long startTime = System.currentTimeMillis();
        canvas.save();
        canvas.rotate(mDegree * mDirection, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        canvas.drawCircle(centerX, centerY, 3 * radarRadius / 7, mPaintRadar);
        canvas.restore();
        mDegree += mRate;
        long stopTime = System.currentTimeMillis();
        long runTime = stopTime - startTime;
        if (mRate != 0) {
            // 16毫秒执行一次
            postInvalidateDelayed(Math.abs(runTime - 8));
        }
    }

    public void start() {
        mRate = mTempRate;
        postInvalidate();
    }

    public void stop() {
        mRate = 0;
    }
}
