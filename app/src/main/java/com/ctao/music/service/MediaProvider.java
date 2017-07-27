package com.ctao.music.service;

import com.ctao.music.model.SongInfo;

import java.util.List;

/**
 * Created by A Miracle on 2017/7/7.
 */
public final class MediaProvider {

    /** 当前播放 */
    public SongInfo getCurrentSong(){
        return MediaManager.getMediaManager().getCurrentSong();
    }

    /** 当前播放模式 */
    public int getPattern(){
        return MediaManager.getMediaManager().getPattern();
    }

    /** 当前播放列表*/
    public List<SongInfo> getPlayList(){
        return MediaManager.getMediaManager().getPlayList();
    }

    /** 当前播放状态*/
    public int getState(){
        return MediaManager.getMediaManager().getState();
    }

    /** 当前播放进度*/
    public long getCurrentMillis(){
        return MediaManager.getMediaManager().getCurrentMillis();
    }
}
