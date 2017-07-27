package com.ctao.music.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.ctao.baselib.Global;
import com.ctao.baselib.utils.BitmapUtils;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.music.App;
import com.ctao.music.AppCache;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.event.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

/**
 * Created by A Miracle on 2016/12/30.
 */
public class UIUtils {
    private static final int sRadius = 13;
    private static int sAlpha = 220;

    public static void setAlpha(int alpha){
        if(sAlpha != alpha){
            sAlpha = alpha > 255 ? 255 : alpha;
            sAlpha = alpha < 0 ? 0 : alpha;
        }
    }

    /**
     * 设置高斯模糊皮肤
     * @param contentView View 使用缓存 异步
     */
    public static void setupSkinBlurry(final View contentView){
        setupSkinBlurry(contentView, true, true);
    }

    /**
     * 设置高斯模糊皮肤
     * @param contentView View 使用缓存 异步
     * @param cache 是否使用缓存
     */
    public static void setupSkinBlurry(final View contentView, boolean cache){
        setupSkinBlurry(contentView, cache, true);
    }

    /**
     * 设置高斯模糊皮肤
     * @param contentView View
     * @param cache 是否使用缓存
     * @param asyn 是否异步
     */
    public static void setupSkinBlurry(final View contentView, boolean cache, boolean asyn){
        final Resources resources = App.getApp().getResources();

        if(cache){
            Bitmap blurryBitmap = AppCache.getInstance().getSkinBlurryBitmap();
            if(blurryBitmap != null){
                contentView.setBackground(getBitmapDrawable(resources, blurryBitmap));
                return;
            }
        }

        Bitmap bitmap = cache ? AppCache.getInstance().getSkinBitmap() : null;
        if(bitmap == null){
            setupSkin(contentView, cache);
            bitmap = AppCache.getInstance().getSkinBitmap();
        }

        if(bitmap != null){
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                bitmap = BitmapUtils.zoomBitmapScale(bitmap, 0.5f, 0.5f);
            }
            final Bitmap finalBitmap = bitmap.copy(bitmap.getConfig(), true);
            if(asyn){
                new Thread() {
                    @Override
                    public void run() {
                        final Bitmap blur = BlurryUtils.blur(finalBitmap, sRadius);
                        if(blur != null){
                            AppCache.getInstance().setSkinBlurryBitmap(blur);
                            Global.getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    contentView.setBackground(getBitmapDrawable(resources, blur));
                                }
                            });
                        }
                    }
                }.start();
            }else{
                final Bitmap blur = BlurryUtils.blur(finalBitmap, sRadius);
                if(blur != null){
                    AppCache.getInstance().setSkinBlurryBitmap(blur);
                    contentView.setBackground(getBitmapDrawable(resources, blur));
                }
            }
        }
    }

    /**
     * 设置皮肤
     * @param contentView View 使用缓存
     */
    public static void setupSkin(final View contentView){
        setupSkin(contentView, true);
    }

    /**
     * 设置皮肤
     * @param contentView View
     * @param cache 是否使用缓存
     */
    public static void setupSkin(final View contentView, boolean cache){
        Resources resources = App.getApp().getResources();

        // 取缓存
        if(cache){
            Bitmap skinBitmap = AppCache.getInstance().getSkinBitmap();
            if(skinBitmap != null){
                contentView.setBackground(getBitmapDrawable(resources, skinBitmap));
                return;
            }
        }


        // path
        String path = SPUtils.getString(Constant.SP_SKIN_PATH, null);
        if(!TextUtils.isEmpty(path)){
            if(path.startsWith(Constant.SKIN_TYPE_FILE)){
                path = path.substring(Constant.SKIN_TYPE_FILE.length());

                File file = new File(path);
                Bitmap bitmap = BitmapUtils.readFile2BitmapZoom(file);
                if (bitmap != null) {
                    AppCache.getInstance().setSkinBitmap(bitmap);
                    AppCache.getInstance().setSkinName(file.getName());
                    contentView.setBackground(getBitmapDrawable(resources, bitmap));
                }else{
                    SPUtils.builder().putString(Constant.SP_SKIN_PATH, null).commit();
                    setupWallpaperSkin(contentView);
                }

            }else if(path.startsWith(Constant.SKIN_TYPE_RES)){
                path = path.substring(Constant.SKIN_TYPE_RES.length());

                int mipmapId = resources.getIdentifier(path, "mipmap", App.getApp().getPackageName());
                if (mipmapId != 0) {
                    Bitmap bitmap = BitmapFactory.decodeResource(resources, mipmapId);
                    AppCache.getInstance().setSkinBitmap(bitmap);
                    AppCache.getInstance().setSkinName(path);
                    contentView.setBackground(getBitmapDrawable(resources, bitmap));
                }else{
                    SPUtils.builder().putString(Constant.SP_SKIN_PATH, null).commit();
                    setupWallpaperSkin(contentView);
                }
            }
        }else{
            setupWallpaperSkin(contentView);
        }

        // 暗箱操作, 异步生成模糊皮肤
        new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = AppCache.getInstance().getSkinBitmap();
                if(bitmap == null){
                    return;
                }
                Bitmap blur = BlurryUtils.blur(bitmap.copy(bitmap.getConfig(), true), sRadius);
                if(blur != null){
                    AppCache.getInstance().setSkinBlurryBitmap(blur);
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_UPDATE_SKIN_BLURRY));
                }
            }
        }.start();
    }

    /**设置系统壁纸*/
    private static void setupWallpaperSkin(View contentView) {
        Resources resources = App.getApp().getResources();

        Bitmap bitmap = WallpaperUtils.getWallpaperBitmap(App.getApp());
        if(bitmap != null) {
            AppCache.getInstance().setSkinBitmap(bitmap);
            AppCache.getInstance().setSkinName(null);
            contentView.setBackground(getBitmapDrawable(resources, bitmap));
        }else{
            String res_default = "wallpaper_default";
            SPUtils.builder().putString(Constant.SP_SKIN_PATH, res_default).commit();
            bitmap = BitmapFactory.decodeResource(resources, R.mipmap.wallpaper_default);
            AppCache.getInstance().setSkinBitmap(bitmap);
            AppCache.getInstance().setSkinName(res_default);
            contentView.setBackground(getBitmapDrawable(resources, bitmap));
        }
    }

    @NonNull
    private static BitmapDrawable getBitmapDrawable(Resources resources, Bitmap blurryBitmap) {
        BitmapDrawable drawable = new BitmapDrawable(resources, blurryBitmap);
        drawable.setAlpha(sAlpha);
        return drawable;
    }
}
