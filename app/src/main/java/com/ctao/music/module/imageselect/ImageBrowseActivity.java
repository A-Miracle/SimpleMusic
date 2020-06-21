package com.ctao.music.module.imageselect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArrayMap;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ctao.baselib.Global;
import com.ctao.baselib.utils.LogUtils;
import com.ctao.baselib.utils.ToastUtils;
import com.ctao.music.R;
import com.ctao.music.module.imageselect.widget.ZoomImageView;
import com.ctao.music.module.imageselect.widget.ZoomViewPager;
import com.ctao.music.ui.base.MvpActivity;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * Created by A Miracle on 2017/2/8.
 */
public class ImageBrowseActivity extends MvpActivity {

    public static final String ExtraArrayPath = "path_array";
    private static final String TAG = ImageBrowseActivity.class.getSimpleName();
    private List<String> mData;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_image_browse;
    }

    @Override
    protected Toolbar getBackToolBar() {
        return toolbar;
    }

    @Override
    protected void onAfterSetContentLayout(Bundle savedInstanceState) {
        setSwipeBackEnable(false);

        Intent intent = getIntent();
        List<String> data = (List<String>) intent.getSerializableExtra(ExtraArrayPath);
        if(data == null){
            return;
        }

        toolbar.setTitle(R.string.action_preview);

        ZoomViewPager viewPager = (ZoomViewPager) findViewById(R.id.viewpager);
        ImagePageAdapter adapter = new ImagePageAdapter();
        mData = adapter.getData();
        adapter.setProgressBar((ProgressBar) findViewById(R.id.progress_bar));
        if(!data.isEmpty()){
            mData.addAll(data);
        }

        viewPager.setAdapter(adapter);

        viewPager.setPageMargin((int) getResources().getDisplayMetrics().density * 10);
    }

    private static class ImagePageAdapter extends PagerAdapter {
        private List<String> mList = new ArrayList<>();
        private static Map<String, String> cache = new ArrayMap<>(); //static 为了能内存缓存数据!
        private ProgressBar progressBar;

        public void setProgressBar(ProgressBar progressBar){
            this.progressBar = progressBar;
        }
        public List<String> getData(){
            return mList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            final ZoomImageView zoomImageView = new ZoomImageView(container.getContext());

            loadImage(zoomImageView, mList.get(position));

            container.addView(zoomImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return zoomImageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);

            try {
                final ImageView imageView = (ImageView) object;
                final Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                imageView.setImageBitmap(null);
                bitmap.recycle();
            } catch (Exception e) {}
        }


        private void loadImage(final ZoomImageView zoomImageView, String data) {
            if(TextUtils.isEmpty(data)){
               return;
            }

            if(data.startsWith("http")){
                Uri uri = Uri.parse(data);
                final String name = uri.getQueryParameter("name");
                String value = cache.get(name);
                if(!TextUtils.isEmpty(value)){
                    if(setImageBitmap(zoomImageView, value)){
                        return;
                    }else{
                        cache.remove(name);
                    }
                }

                showProgress();
                Glide.with(Global.getContext()).load(data)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .centerCrop()
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                hideProgress();
                                ToastUtils.show("加载失败");
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                hideProgress();
                                return false;
                            }
                        })
                        .into(zoomImageView);

            }else if(data.startsWith("file://")){
                String file = data.substring("file://".length());
                setImageBitmap(zoomImageView, file);
            }else{
                setImageBitmap(zoomImageView, data);
            }
        }

        private boolean setImageBitmap(ZoomImageView zoomImageView, String file) {
            if(TextUtils.isEmpty(file)){
                return false;
            }

            try {
                FileInputStream fis = new FileInputStream(file);
                FileDescriptor fd = fis.getFD();
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inDither = false;
                opts.inPurgeable = true;
                opts.inInputShareable = true;
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd, null, opts);
                if(bitmap != null){
                    zoomImageView.setImageBitmap(bitmap);
                    return true;
                }
            } catch (IOException e) {
                LogUtils.e(TAG, e.getMessage());
            }
            return false;
        }

        public void showProgress() {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        public void hideProgress() {
            if(progressBar != null){
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
