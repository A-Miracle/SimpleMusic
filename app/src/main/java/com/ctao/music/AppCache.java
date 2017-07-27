package com.ctao.music;

import android.graphics.Bitmap;

import java.lang.ref.WeakReference;

/**
 * Created by A Miracle on 2017/7/25.
 */
public class AppCache {
    private AppCache() {}
    private static class Single {
        static AppCache Instance = new AppCache();
    }
    public static AppCache getInstance() {
        return AppCache.Single.Instance;
    }

    /**当前选中的壁纸名称*/
    private String mSkinName;

    /**皮肤, 壁纸缓存*/
    private WeakReference<Bitmap> mSkinBitmap;

    /**皮肤, 壁纸 高斯模糊 缓存*/
    private WeakReference<Bitmap> mSkinBlurryBitmap;

    /** Wallpaper 缓存*/
    private WeakReference<Bitmap> mWallpaperBitmap;

    public Bitmap getWallpaperBitmap(){
        if(mWallpaperBitmap != null){
            return mWallpaperBitmap.get();
        }
        return null;
    }

    public void setWallpaperBitmap(Bitmap bitmap){
        mWallpaperBitmap = new WeakReference<>(bitmap);
    }

    public Bitmap getSkinBitmap(){
        if(mSkinBitmap != null){
            return mSkinBitmap.get();
        }
        return null;
    }

    public void setSkinBitmap(Bitmap bitmap){
        mSkinBitmap = new WeakReference<>(bitmap);
    }

    public Bitmap getSkinBlurryBitmap(){
        if(mSkinBlurryBitmap != null){
            return mSkinBlurryBitmap.get();
        }
        return null;
    }

    public void setSkinBlurryBitmap(Bitmap bitmap){
        mSkinBlurryBitmap = new WeakReference<>(bitmap);
    }

    public String getSkinName() {
        return mSkinName;
    }

    public void setSkinName(String skinName) {
        this.mSkinName = skinName;
    }
}
