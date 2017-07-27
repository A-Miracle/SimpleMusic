package com.ctao.music.module.imageselect.bean;

import android.text.TextUtils;

import com.ctao.baselib.utils.LogUtils;

/**
 * 图片实体
 * Created by A Miracle on 2016/10/28.
 */
public class Image {
    public String path;
    public String name;
    public long time;

    public Image(String path, String name, long time){
        this.path = path;
        this.name = name;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        try {
            Image other = (Image) o;
            return TextUtils.equals(this.path, other.path);
        }catch (ClassCastException e){
            LogUtils.e(e);
        }
        return super.equals(o);
    }
}
