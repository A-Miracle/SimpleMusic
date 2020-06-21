package com.ctao.music.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.ctao.music.AppConfig;
import com.ctao.music.callback.IPlayState;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.model.SongInfo;
import com.ctao.music.receiver.RemoteControlReceiver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by A Miracle on 2017/6/27.
 */
public final class MediaPlayerService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener {
    private MediaNotificationManager mMediaNotificationManager;
    private MediaManager mMediaManager;
    private IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private NoisyAudioStreamReceiver mNoisyReceiver = new NoisyAudioStreamReceiver();
    private boolean isRegisterNoisy;
    private AudioManager mAudioManager;
    private ComponentName mRemoteReceiver;

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mMediaNotificationManager.stopNotification();
        new MediaManager.Caretaker().archive(mMediaManager.createMemo()); // 存档
        if (mRemoteReceiver != null) {
            mAudioManager.unregisterMediaButtonEventReceiver(mRemoteReceiver);
        }
        super.onDestroy();
        if(!AppConfig.APP_CLOSE){
            // 在此重新启动,使服务常驻内存
            startService(new Intent(this, MediaPlayerService.class));
        }
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        super.onCreate();
        mMediaNotificationManager = new MediaNotificationManager(this);
        mMediaManager = MediaManager.getMediaManager();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mRemoteReceiver = new ComponentName(getPackageName(), RemoteControlReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mRemoteReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
    }

    public boolean isPlaying(){ // 因为准备是异步的
        return mMediaManager.getState() == IPlayState.STATE_PREPARING || mMediaManager.isPlaying();
    }

    public SongInfo getCurrentSong(){
        return mMediaManager.getCurrentSong();
    }

    public long getCurrentMillis(){
        return mMediaManager.getCurrentMillis();
    }

    // 所有播放操作都在此
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        SongInfo songInfo;
        switch (event.getType()){
            case MessageEvent.MUSIC_LIST_INIT: // 初始化播放列表
                List<SongInfo> data = (List<SongInfo>) event.getParam();
                mMediaManager.initPlayList(data);

                if(null != mMediaManager.getCurrentSong()){
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_CHANGE_SONG, mMediaManager.getCurrentSong()));
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_CHANGE_STATE, mMediaManager.getState()));
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_UPDATE_PROGRESS, new long[]{mMediaManager.getCurrentMillis(), mMediaManager.getCurrentSong().getDurationMs()}));
                    mMediaNotificationManager.startNotification(); // 开启通知栏
                }
                break;
            case MessageEvent.MUSIC_PLAY_OR_PAUSE: // 播放或暂停
                songInfo = (SongInfo) event.getParam();
                if(null == songInfo || songInfo.equals(mMediaManager.getCurrentSong())){
                    // 是当前播放歌曲, 操作播放或暂停
                    if(mMediaManager.isPlaying()){
                        pause();
                    }else{
                        play(null);
                    }
                }else{
                    // 非当前播放歌曲, 切歌
                    play(songInfo);
                }
                break;
            case MessageEvent.MUSIC_PRE: // 上一首
                mMediaManager.pre();
                break;
            case MessageEvent.MUSIC_NEXT: // 下一首
                mMediaManager.next();
                break;
            case MessageEvent.MUSIC_PATTERN: // 播放模式
                int pattern = (int) event.getParam();
                mMediaManager.setPattern(pattern);
                break;
            case MessageEvent.MUSIC_SEEK_TO: // 播放位置
                long seekTo = (long) event.getParam();
                mMediaManager.seekTo(seekTo);
                break;
            case MessageEvent.MUSIC_PLAY: // 播放
                if(!mMediaManager.isPlaying()){
                    play(null);
        }
                break;
            case MessageEvent.MUSIC_PAUSE: // 暂停
                if(mMediaManager.isPlaying()){
                    pause();
                }
                break;
        }
    }

    private void play(SongInfo songInfo){
        if(null == songInfo){
            mMediaManager.play();
        }else{
            mMediaManager.play(songInfo);
        }
        if(!isRegisterNoisy){
            isRegisterNoisy = true;
            registerReceiver(mNoisyReceiver, mNoisyFilter);
            mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN); //请求焦点[来电也在此处理]
        }
    }

    private void pause(){
        mMediaManager.pause();
        unregisterReceiver(mNoisyReceiver);
        mAudioManager.abandonAudioFocus(this); //放弃焦点
        isRegisterNoisy = false;
    }

    //----------------MediaBrowserServiceCompat
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                pause();
                break;
        }
    }

    /**
     * 耳机拔出时暂停播放
     */
    static class NoisyAudioStreamReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PAUSE));
        }
    }
}
