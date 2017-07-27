package com.ctao.music.module.imageselect.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctao.baselib.ui.adpter.holder.LViewHolder;
import com.ctao.music.App;
import com.ctao.music.R;
import com.ctao.music.module.imageselect.bean.Folder;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by A Miracle on 2016/10/28.
 */
public class FolderAdapter extends BaseAdapter {

    private Context mContext = App.getApp();
    private List<Folder> mFolders = new ArrayList<>();
    private int mSelectedIndex = 0;

    public FolderAdapter(){}

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        if(mSelectedIndex == selectedIndex){
            return;
        }
        mSelectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    public void setData(List<Folder> folders) {
        mFolders.clear();
        if(folders != null && folders.size()>0){
            mFolders.addAll(folders);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFolders.size() + 1; //+所有图片选项
    }

    @Override
    public Folder getItem(int position) {
        if(position == 0)
            return null;
        return mFolders.get(position-1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LViewHolder holder = LViewHolder.get(convertView, R.layout.item_folder, position, parent);
        bindData(holder, getItem(position), position);
        return holder.getRootView();
    }

    private void bindData(LViewHolder holder, Folder folder, int position) {
        ImageView item_iv_cover = holder.getView(R.id.item_iv_cover);
        TextView item_tv_name = holder.getView(R.id.item_tv_name);
        TextView item_tv_path = holder.getView(R.id.item_tv_path);
        TextView item_tv_size = holder.getView(R.id.item_tv_size);
        ImageView item_iv_check = holder.getView(R.id.item_iv_check);

        if(position == 0){ //全部图片
            item_tv_name.setText(R.string.folder_all);
            item_tv_path.setText("/sdcard");
            item_tv_size.setText(String.format("%d%s", getTotalImageSize(), mContext.getString(R.string.photo_unit)));

            if (mFolders.size() > 0) {
                Folder f = mFolders.get(0);
                if (f != null) {
                    Picasso.with(mContext)
                            .load(new File(f.cover.path))
                            .placeholder(R.mipmap.ic_default_error)
                            .error(R.mipmap.ic_default_error)
                            .resizeDimen(R.dimen.folder_cover_size, R.dimen.folder_cover_size)
                            .centerCrop()
                            .into(item_iv_cover);
                } else {
                    item_iv_cover.setImageResource(R.mipmap.ic_default_error);
                }
            }
        }else {
            if(folder == null){
                return;
            }

            item_tv_name.setText(folder.name);
            item_tv_path.setText(folder.path);
            if (folder.images != null) {
                item_tv_size.setText(String.format("%d%s", folder.images.size(), mContext.getString(R.string.photo_unit)));
            }else{
                item_tv_size.setText("*"+mContext.getString(R.string.photo_unit));
            }

            if (folder.cover != null) {
                // 显示图片
                Picasso.with(mContext)
                        .load(new File(folder.cover.path))
                        .placeholder(R.mipmap.ic_default_error)
                        .error(R.mipmap.ic_default_error)
                        .resizeDimen(R.dimen.folder_cover_size, R.dimen.folder_cover_size)
                        .centerCrop()
                        .into(item_iv_cover);
            }else{
                item_iv_cover.setImageResource(R.mipmap.ic_default_error);
            }
        }
        if(mSelectedIndex == position){
            item_iv_check.setVisibility(View.VISIBLE);
        }else{
            item_iv_check.setVisibility(View.INVISIBLE);
        }
    }

    private int getTotalImageSize(){
        int result = 0;
        if(mFolders != null && mFolders.size()>0){
            for (Folder f: mFolders){
                result += f.images.size();
            }
        }
        return result;
    }
}
