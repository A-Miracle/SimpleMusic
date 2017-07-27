package com.ctao.music.callback;

/**
 * Created by A Miracle on 2017/7/1.
 */
public interface IPlayState {
    /** 闲置状态 */
    int STATE_IDLE = 0;
    /** 初始化状态 */
    int STATE_INITIALIZED = 1;
    /** 准备中状态 */
    int STATE_PREPARING = 2;
    /** 准备状态 */
    int STATE_PREPARED = 3;
    /** 播放状态 */
    int STATE_PLAYING = 4;
    /** 暂停状态 */
    int STATE_PAUSED = 5;
    /** 停止状态 */
    int STATE_STOPPED  = 6;
    /** 播放完成状态 */
    int STATE_PLAYBACK_COMPLETED = 7;
    /** 错误状态 */
    int STATE_ERROR  = 8;
    /** 结束状态 */
    int STATE_END  = 9;

    /** 获取状态 */
    int getState();
}
