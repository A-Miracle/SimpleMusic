package com.ctao.music.module.imageselect.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ctao.baselib.ui.adpter.holder.LViewHolder;
import com.ctao.baselib.utils.DisplayUtils;
import com.ctao.music.App;
import com.ctao.music.R;
import com.ctao.music.module.imageselect.ImageSelectFragment;
import com.ctao.music.module.imageselect.bean.Image;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by A Miracle on 2016/10/28.
 */
public class ImageGridAdapter extends BaseAdapter {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;

    private Context mContext = App.getApp();

    private boolean isShowCamera = true;
    private boolean isShowSelectIndicator = true;

    private List<Image> mImages = new ArrayList<>();
    private List<Image> mSelectedImages = new ArrayList<>();
    private int mGridWidth;

    public ImageGridAdapter(boolean isShowCamera, int column){
        this.isShowCamera = isShowCamera;
        int widthPixels = DisplayUtils.width;
        mGridWidth = widthPixels / column;
    }

    public boolean isShowCamera() {
        return isShowCamera;
    }

    public void setShowCamera(boolean showCamera) {
        if(isShowCamera == showCamera){
            return;
        }
        isShowCamera = showCamera;
        notifyDataSetChanged();
    }

    public boolean isShowSelectIndicator() {
        return isShowSelectIndicator;
    }

    public void setShowSelectIndicator(boolean showSelectIndicator) {
        if(isShowSelectIndicator == showSelectIndicator){
            return;
        }
        isShowSelectIndicator = showSelectIndicator;
        notifyDataSetChanged();
    }

    /**选择某个图片，改变选择状态*/
    public void select(Image image) {
        if(mSelectedImages.contains(image)){
            mSelectedImages.remove(image);
        }else{
            mSelectedImages.add(image);
        }
        notifyDataSetChanged();
    }

    /** 通过图片路径设置默认选择 */
    public void setDefaultSelected(ArrayList<String> resultList) {
        for(String path : resultList){
            Image image = getImageByPath(path);
            if(image != null){
                mSelectedImages.add(image);
            }
        }
        if(mSelectedImages.size() > 0){
            notifyDataSetChanged();
        }
    }

    private Image getImageByPath(String path){
        if(mImages != null && mImages.size()>0){
            for(Image image : mImages){
                if(image.path.equalsIgnoreCase(path)){
                    return image;
                }
            }
        }
        return null;
    }

    /** 设置数据集 */
    public void setData(List<Image> images) {
        mSelectedImages.clear();
        mImages.clear();

        if(images != null && images.size()>0){
            mImages.addAll(images);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(isShowCamera){
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getCount() {
        return isShowCamera ? mImages.size()+1 : mImages.size();
    }

    @Override
    public Image getItem(int position) {
        if(isShowCamera){
            if(position == 0){
                return null;
            }
            return mImages.get(position - 1);
        }else{
            return mImages.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemViewType = getItemViewType(position);
        int layoutId;
        if(itemViewType == TYPE_CAMERA){
            layoutId = R.layout.item_camera;
        }else{
            layoutId = R.layout.item_image;
        }
        LViewHolder holder = LViewHolder.get(convertView, layoutId, position, parent);
        bindData(holder, getItem(position), itemViewType);
        return holder.getRootView();
    }

    private void bindData(LViewHolder holder, Image image, int type) {
        if(type == TYPE_CAMERA){
            return;
        }
        if(image == null){
            return;
        }

        ImageView imageView = holder.getView(R.id.item_iv_image);
        View mask = holder.getView(R.id.item_v_prospects);
        ImageView indicator = holder.getView(R.id.item_iv_check);

        // 处理单选和多选状态
        if(isShowSelectIndicator){
            indicator.setVisibility(View.VISIBLE);
            if(mSelectedImages.contains(image)){
                // 设置选中状态
                indicator.setImageResource(R.mipmap.btn_select_on);
                mask.setVisibility(View.VISIBLE);
            }else{
                // 未选择
                indicator.setImageResource(R.mipmap.btn_select_off);
                mask.setVisibility(View.GONE);
            }
        }else{
            indicator.setVisibility(View.GONE);
        }

        // 显示图片
        File imageFile = new File(image.path);
        if (imageFile.exists()) {
            Picasso.with(mContext)
                    .load(imageFile)
                    .placeholder(R.mipmap.ic_default_error)
                    .error(R.mipmap.ic_default_error)
                    .tag(ImageSelectFragment.TAG)
                    .resize(mGridWidth, mGridWidth)
                    .centerCrop()
                    .into(imageView);
        }else{
            imageView.setImageResource(R.mipmap.ic_default_error);
        }
    }
}