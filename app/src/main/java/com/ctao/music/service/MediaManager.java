package com.ctao.music.service;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.ctao.baselib.utils.FileUtils;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.baselib.utils.ToastUtils;
import com.ctao.music.callback.IPlay;
import com.ctao.music.callback.IPlayPattern;
import com.ctao.music.callback.MediaPlay;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.model.SongInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by A Miracle on 2017/6/28.
 */
final class MediaManager implements IPlay.Callback, IPlayPattern {
    private static final int MUSIC_PRE = 0xA;
    private static final int MUSIC_NEXT = 0xB;

    private List<SongInfo> _playList; // 当前播放列表
    private int _pointer = -1; // 指针
    private int _pattern = PATTERN_CYCLE; // 播放模式

    private SongInfo _currentSong; // 当前播放歌曲
    private long _currentMillis; // 当前播放歌曲进度

    private Random _random; // Random

    private MediaManager(){
        _playList = new ArrayList<>();
        _random = new Random();
        getPlayer().setCallback(this);
    }

    private Handler _handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MUSIC_PRE:
                    _pre();
                    break;
                case MUSIC_NEXT:
                    _next();
                    break;
            }
        }
    };

    public static MediaManager getMediaManager() {
        return Single._mediaManager;
    }

    private static class Single{
        private static MediaManager _mediaManager = new MediaManager();
    }

    public void initPlayList(List<SongInfo> palyList){
        if(null != palyList && palyList.size() > 0){
            _playList.clear();
            _playList.addAll(palyList);

            SongInfo source = getPlayer().getSource();
            if(null != source && _playList.contains(source)){
                _pointer = _playList.indexOf(source);
                _currentSong = source;
                _currentMillis = getPlayer().getCurrentMillis();
            }else{
                if(_pointer == -1){
                    // 需要使用备忘录模式记录上次播放, 播放模式, 播放列表
                    Memo memo = new Caretaker().getMemo();
                    source = new SongInfo(memo.songId);
                    int index = _playList.indexOf(source);
                    if(index != -1){
                        _pointer = index;
                        _currentSong = _playList.get(index);
                        _currentMillis = memo.seekTo;
                        _pattern = memo.pattern;
                    }else{
                        _pointer = 0;
                        _currentSong = _playList.get(0);
                    }
                }
            }
        }
    }

    public List<SongInfo> getPlayList(){
        if(null != _playList){
            return new ArrayList<>(_playList);
        }
        return null;
    }

    public void setPattern(int pattern) {
         _pattern = pattern;
    }

    @Override
    public int getPattern() {
        return _pattern;
    }

    public long getCurrentMillis(){
        return _currentMillis;
    }

    public SongInfo getCurrentSong(){
        if(null != _currentSong){
            return _currentSong.clone();
        }
        return null;
    }

    public boolean isPlaying() {
        return getPlayer().isPlaying();
    }

    public void play() {
        if(null != _currentSong){
            play(_currentSong);
        }
    }

    public void play(SongInfo source) {
        long ms = 0;
        if(null != _currentSong && _currentSong.equals(source)){
            ms = _currentMillis; // 从暂停位置开始播放
            if(getPlayer().start()){
                return;
            }
        }else{
            _currentSong = source;
            if(null != _playList){
                if(!_playList.contains(_currentSong)){ // 不包含, 添加
                    _playList.add(++_pointer, _currentSong);
                }else{ // 包含, 更改指针位置
                    _pointer = _playList.indexOf(_currentSong);
                }
            }

            // 更换歌曲
            EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_CHANGE_SONG, source));
        }

        // 检查文件是否存在需要自行判断, 播放器并不会有任何提示
        if(!FileUtils.isExists(source.getSource())){
            ToastUtils.show("歌曲文件不存在, 自动跳至下一首");
            next();
            return;
        }

        getPlayer().play(source, ms);
    }

    public void pre() {
        _handler.sendEmptyMessageDelayed(MUSIC_PRE, 200);
    }

    public void next() {
        _handler.sendEmptyMessageDelayed(MUSIC_NEXT, 200);
    }

    public void pause() {
        getPlayer().pause();
    }

    public void stop() {
        getPlayer().stop();
    }

    public void seekTo(long ms) {
        _currentMillis = ms;
        getPlayer().seekTo(ms);
    }

    public int getState() {
        return getPlayer().getState();
    }

    private boolean checkPlayList(){
        if(null == _playList || _playList.size() <= 0){
            ToastUtils.show("当前播放列表无歌曲!");
            return false;
        }
        return true;
    }

    private IPlay<SongInfo> getPlayer(){
        return MediaPlay.getMediaPlay();
    }

    private void _pre() {
        if(!checkPlayList()){
            return;
        }
        _currentMillis = 0;
        switch (_pattern){
            case PATTERN_RANDOM:
                _pointer = _random.nextInt(_playList.size());
                break;
            default: // 手动点击上一首, 除随机外都一样
                _pointer = --_pointer < 0 ? _playList.size() - 1 : _pointer;
                break;
        }
        play(_playList.get(_pointer));
    }

    private void _next() {
        if(!checkPlayList()){
            return;
        }
        _currentMillis = 0;
        switch (_pattern){
            case PATTERN_RANDOM:
                _pointer = _random.nextInt(_playList.size());
                break;
            default: // 手动点击下一首, 除随机外都一样
                _pointer = ++_pointer >= _playList.size() ? 0 : _pointer;
                break;
        }
        play(_playList.get(_pointer));
    }

    // ---------------------IPlay.Callback

    @Override
    public void onProgress(long currentMillis, long duration) {
        _currentMillis = currentMillis;
        EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_UPDATE_PROGRESS, new long[]{currentMillis, _currentSong.getDurationMs()}));

    }

    @Override
    public void onCompletion() {
        switch (_pattern){
            case PATTERN_LIST:
                // 到最后一首了, 停止播放
                if(_pointer == _playList.size() - 1){
                    seekTo(0);
                    stop();
                }
                break;
            case PATTERN_SINGLE:
                // 单曲循环时需清零
                seekTo(0);
                getPlayer().start();
                break;
            case PATTERN_CYCLE:
            case PATTERN_RANDOM:
                next(); // 播完, 下一首
                break;
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_CHANGE_STATE, state));
    }

    @Override
    public void onError(CharSequence error) {
        String msg = error.toString();
        if(TextUtils.isEmpty(msg)){
            msg = "发生未知错误! 即将播放下一首";
        }
        ToastUtils.show(msg);
        next(); // 出错, 下一首
    }

    // 创建备忘录
    public Memo createMemo(){
        Memo memo = new Memo();
        memo.songId = _currentSong.getId();
        memo.seekTo = _currentMillis;
        memo.pattern = _pattern;
        return memo;
    }

    // Caretaker, 负责管理Memo
    static class Caretaker{

        // 存档
        public void archive(Memo memo){
            SPUtils.builder()
                    .putString("songId", memo.songId)
                    .putLong("seekTo", memo.seekTo)
                    .putInt("pattern", memo.pattern).commit();
        }

        // 获取存档
        public Memo getMemo(){
            Memo memo = new Memo();
            memo.songId = SPUtils.getString("songId", "");
            memo.seekTo = SPUtils.getLong("seekTo", 0);
            memo.pattern = SPUtils.getInt("pattern", 0);
            return memo;
        }
    }

    // 备忘录类
    static class Memo{
        String songId; // 当前播放[SongInfo通过id判断相等的, 所以存id就够了]
        long seekTo; // 播放进度
        int pattern; // 播放模式
        // TODO 当前播放列表
    }
}
