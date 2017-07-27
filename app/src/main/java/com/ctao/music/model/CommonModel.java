package com.ctao.music.model;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by A Miracle on 2017/1/2.
 * 常用的Model
 */
public class CommonModel {

    private int iconId; // 头像, 图片资源Id
    private File iconFile; // 头像, path
    private String iconUrl; // 头像, url
    private Bitmap iconBitmap; // 头像, Bitmap

    private String itemContent; // item的内容
    private String itemTitle; // item的标题

    private int type; //item类型

    private boolean selected; //针对复选框, item是否被选中

    private String tag; // 标记

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getItemContent() {
        return itemContent;
    }

    public void setItemContent(String itemContent) {
        this.itemContent = itemContent;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    public void setIconBitmap(Bitmap iconBitmap) {
        this.iconBitmap = iconBitmap;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public File getIconFile() {
        return iconFile;
    }

    public void setIconFile(File iconFile) {
        this.iconFile = iconFile;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
