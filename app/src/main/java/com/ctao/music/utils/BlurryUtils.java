package com.ctao.music.utils;

import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.ctao.baselib.lib.blurry.internal.Blur;
import com.ctao.music.App;

/**
 * 图片模糊特效
 * Created by A Miracle on 2016/12/28.
 */
public class BlurryUtils {

    public static Bitmap blur(Bitmap bitmap, int radius){
        try {
            if(blurRenderScript(bitmap, radius)){
                return bitmap;
            }
        } catch (Exception e) {
        }
        return blurStack(bitmap, radius);
    }

    private static Bitmap blurStack(Bitmap bitmap, int radius){
        return Blur.stack(bitmap, radius, true);
    }

    private static boolean blurRenderScript(Bitmap bitmap, int radius){
        // 感觉4.4的模糊的不是很好
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            RenderScript rs = RenderScript.create(App.getApp());
            Allocation overlayAlloc = Allocation.createFromBitmap(rs, bitmap);
            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());

            blur.setInput(overlayAlloc);
            blur.setRadius(radius);
            blur.forEach(overlayAlloc);

            overlayAlloc.copyTo(bitmap);
            rs.destroy();
            return true;
        }
        return false;
    }
}
