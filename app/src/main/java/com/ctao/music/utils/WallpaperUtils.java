package com.ctao.music.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.ctao.baselib.utils.BitmapUtils;
import com.ctao.baselib.utils.DisplayUtils;
import com.ctao.baselib.utils.LogUtils;
import com.ctao.music.AppCache;

/**
 * Created by A Miracle on 2016/12/27.
 */
public class WallpaperUtils {
    public static Bitmap getWallpaper(Context context) {
        Drawable drawable = WallpaperManager.getInstance(context).getDrawable();
        return BitmapUtils.getBitmapFromDrawable(drawable);
    }

    public static Bitmap getWallpaperBitmap(Context context) {
        Bitmap bitmap = AppCache.getInstance().getWallpaperBitmap();
        if(bitmap != null){
            return bitmap;
        }

        bitmap = getWallpaper(context);

        int intrinsicWidth = bitmap.getWidth();
        int intrinsicHeight = bitmap.getHeight();

        int clipWidth = 0;
        float clipRatio = 1f;
        // 一般宽屏处理, 取居中
        if(intrinsicWidth > intrinsicHeight){
            float intrinsicRatio = intrinsicWidth * 1.0f / intrinsicHeight;
            float displayRatio = DisplayUtils.width * 1.0f / DisplayUtils.height;
            float ratio = intrinsicRatio / displayRatio;

            if(ratio > 1){ // 宽屏壁纸, 裁剪, ratio为宽屏倍数
                float realWidth = intrinsicWidth / ratio; // 实际一屏宽度
                clipWidth = (int) ((intrinsicWidth - realWidth) / 2);
                clipRatio = realWidth / DisplayUtils.width;
            }
        }

        LogUtils.printOut("DisplayUtils.width : " + DisplayUtils.width);
        LogUtils.printOut("DisplayUtils.height : " + DisplayUtils.height);

        Bitmap newBitmap = Bitmap.createBitmap(bitmap, clipWidth, 0, (int) (DisplayUtils.width * clipRatio), intrinsicHeight);

        if(newBitmap != null){
            AppCache.getInstance().setWallpaperBitmap(newBitmap);
        }

        LogUtils.printOut("clipWidth : "+clipWidth);
        LogUtils.printOut("clipRatio : "+clipRatio);
        LogUtils.printOut("bitmap : " + intrinsicWidth + " , " + intrinsicHeight);
        LogUtils.printOut("newBitmap : " + newBitmap.getWidth() + " , " + newBitmap.getHeight());
        return newBitmap;
    }
}
