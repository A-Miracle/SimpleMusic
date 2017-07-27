package com.ctao.music.module.imageselect.bean;

import android.text.TextUtils;

import com.ctao.baselib.utils.LogUtils;

import java.util.List;

/**
 * 文件夹
 * Created by A Miracle on 2016/10/28.
 */
public class Folder {
    public String name;
    public String path;
    public Image cover;
    public List<Image> images;

    @Override
    public boolean equals(Object o) {
        try {
            Folder other = (Folder) o;
            return TextUtils.equals(other.path, path);
        }catch (ClassCastException e){
            LogUtils.e(e);
        }
        return super.equals(o);
    }
}
