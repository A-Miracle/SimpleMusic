package com.ctao.music.callback;


/**
 * Created by A Miracle on 2017/6/28.
 */
public interface IPlay <Source> extends IPlayState{

    /**是否正在播放*/
    boolean isPlaying();

    /**获取当前源*/
    Source getSource();

    /**播放指定源*/
    void play(Source source);

    /**播放指定源, 和从哪里开始播放*/
    void play(Source source, long ms);

    /**暂停, 可以从暂停位置恢复*/
    void pause();

    /**开始, 可以从暂停位置恢复*/
    boolean start();

    /**停止, 可重新播放*/
    void stop();

    /**歌曲总时长*/
    long getDuration();

    /**当前播放*/
    long getCurrentMillis();

    /**指定位置播放*/
    void seekTo(long ms);

    /** 重置 */
    void reset();

    /** 释放 */
    void release();

    interface Callback {
        /** 播放进度 */
        void onProgress(long currentMillis, long duration);

        /** 在当前的音乐完成。 */
        void onCompletion();

        /** 播放状态改变 */
        void onPlaybackStatusChanged(int state);

        /** @param error error */
        void onError(CharSequence error);
    }

    void setCallback(Callback callback);
}
