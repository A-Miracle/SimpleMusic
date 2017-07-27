package com.ctao.music.event;

/**
 * Created by A Miracle on 2017/6/27.
 */
public class MessageEvent{
    private int type;
    private Object param;
    public MessageEvent(int type) {
        this.type = type;
    }
    public MessageEvent(int type, Object param) {
        this.type = type;
        this.param = param;
    }
    public int getType(){
        return this.type;
    }
    public Object getParam(){
        return this.param;
    }

    public static final int MUSIC_PRE = 0xA; // 上一首
    public static final int MUSIC_NEXT = 0xB; // 下一首
    public static final int MUSIC_LIST_INIT = 0xC; // 播放列表初始化[List<SongInfo>]
    public static final int MUSIC_PLAY_OR_PAUSE = 0xD; // 播放或暂停[(SongInfo)]
    public static final int MUSIC_PLAY = 0xE; // 播放
    public static final int MUSIC_PAUSE = 0xF; // 暂停

    public static final int MUSIC_CHANGE_SONG = 0xAA; // 播放歌曲变更[SongInfo]
    public static final int MUSIC_CHANGE_STATE = 0xAB; // 播放状态变更[int State]
    public static final int MUSIC_UPDATE_PROGRESS = 0xAC; // 播放进度[int[] size = 2]
    public static final int MUSIC_CLOSE_DRAWERS = 0xAD; // 关闭DrawerLayout
    public static final int MUSIC_PATTERN = 0xAE; // 播放模式[int]
    public static final int MUSIC_SEEK_TO = 0xAF; // SeekTo[int]
    public static final int MUSIC_SCAN = 0xBA; // 歌曲扫描[List<SongInfo>]
    public static final int MUSIC_LRC_SETTING = 0xBB; // 歌词设置

    public static final int MUSIC_UPDATE_PENDANT = 0xBC; // 挂件变更
    public static final int MUSIC_UPDATE_SKIN = 0xBD; // 壁纸变更
    public static final int MUSIC_UPDATE_SKIN_BLURRY = 0xBE; // 壁纸变更
    public static final int MUSIC_UPDATE_SKIN_ALPHA = 0xBF; // 壁纸透明度
    public static final int MUSIC_UPDATE_SKIN_ON_OFF = 0xCA; // 壁纸开启关闭[boolean]
}
