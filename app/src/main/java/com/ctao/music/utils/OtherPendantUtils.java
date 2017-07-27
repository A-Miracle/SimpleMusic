package com.ctao.music.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.ctao.baselib.utils.DisplayUtils;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.baselib.widget.snowfall.SnowView;
import com.ctao.music.Constant;
import com.ctao.music.R;

/**
 * Created by A Miracle on 2017/7/24.
 */
public class OtherPendantUtils {

    /**添加挂件*/
    public static void addPendant(Activity activity, View contentView) {
        String pendant = SPUtils.getString(Constant.SP_PENDANT, null);
        if(!TextUtils.isEmpty(pendant)){
            int count;
            int level;
            ViewGroup mContentView = (ViewGroup) contentView;
            ViewGroup.LayoutParams params;
            View pendantView;
            switch (pendant){
                case Constant.PENDANT_SNOW:
                    //read count
                    count = SPUtils.getInt(Constant.SP_SNOW_COUNT, 0);
                    level = SPUtils.getInt(Constant.SP_SNOW_LEVEL, 0);

                    //create
                    pendantView = new SnowView(activity, count);
                    pendantView.setId(R.id.other_pendant);
                    params = new ViewGroup.LayoutParams(DisplayUtils.width, DisplayUtils.height);
                    pendantView.setLayoutParams(params);

                    //add
                    mContentView.addView(pendantView, level == 0 ? 0 : -1);
                    break;
            }
        }
    }
}
