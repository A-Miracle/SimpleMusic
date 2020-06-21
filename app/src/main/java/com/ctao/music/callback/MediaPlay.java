package com.ctao.music.callback;

import android.media.MediaPlayer;
import android.os.SystemClock;

import com.ctao.music.model.SongInfo;

import java.io.IOException;

/**
 * Created by A Miracle on 2017/7/1.
 */
public class MediaPlay implements IPlay<SongInfo>{
    private MediaStatePlayer mPlayer;
    private SongInfo mCurrent;
    private Callback mCallback;

    private boolean isRunning; // 控制子线程
    private Thread playerThread; // 进度监控子线程

    private MediaPlay(){
        init();
    }
    private static class Single{
        private static MediaPlay _instance = new MediaPlay();
    }
    public static MediaPlay getMediaPlay() {
        return Single._instance;
    }

    private void init() {
        mPlayer = new MediaStatePlayer();
        mPlayer.setLooping(false);
        mPlayer.setOnPreparedListener(mPreparedListener);
        mPlayer.setOnCompletionListener(mCompletionListener);
        mPlayer.setOnErrorListener(mErrorListener);
    }

    @Override
    public long getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public boolean start() {
        switch (getState()){
            case STATE_PREPARED:
            case STATE_PLAYING:
            case STATE_PAUSED:
            case STATE_PLAYBACK_COMPLETED:
                mPlayer.start();
                startThread();
                if(null != mCallback){
                    mCallback.onPlaybackStatusChanged(getState());
                }
                return true;
        }
        return false;
    }

    @Override
    public void play(SongInfo songInfo) {
        play(songInfo, 0);
    }

    @Override
    public void play(SongInfo songInfo, long ms) {
        mCurrent = songInfo.clone();
        reset();
        try {
            mPlayer.setDataSource(songInfo.getSource());
            mPlayer.prepareAsync();
            mPlayer.setOffset(ms); //由于prepareAsync异步,这里不能设置seekTo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        switch (getState()){
            case STATE_PLAYING:
            case STATE_PAUSED:
                mPlayer.pause();
                stopThread();
                if(null != mCallback){
                    mCallback.onPlaybackStatusChanged(getState());
                }
                break;
        }
    }

    @Override
    public void stop() {
        switch (getState()){
            case STATE_PREPARED:
            case STATE_PLAYING:
            case STATE_PAUSED:
            case STATE_STOPPED:
            case STATE_PLAYBACK_COMPLETED:
                mPlayer.stop();
                stopThread();
                if(null != mCallback){
                    mCallback.onPlaybackStatusChanged(getState());
                }
                break;
        }
    }

    @Override
    public long getCurrentMillis() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(long ms) {
        switch (getState()){
            case STATE_PREPARED:
            case STATE_PLAYING:
            case STATE_PAUSED:
            case STATE_STOPPED:
            case STATE_PLAYBACK_COMPLETED:
                mPlayer.seekTo((int) ms);
                break;
        }
    }

    @Override
    public void reset() {
        mPlayer.reset();
    }

    @Override
    public void release() {
        mPlayer.release();
    }

    @Override
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public int getState() {
        return mPlayer.getState();
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mPlayer.setState(IPlayState.STATE_PREPARED);
            mPlayer.seekTo((int)mPlayer.getOffset());
            start();
            if(null != mCallback){
                mCallback.onPlaybackStatusChanged(getState());
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mPlayer.setState(IPlayState.STATE_PLAYBACK_COMPLETED);
            // 播放完成
            if(null != mCallback){
                mCallback.onCompletion();
            }
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mPlayer.setState(IPlayState.STATE_ERROR);
            // 播放出错
            if(what == MediaPlayer.MEDIA_ERROR_SERVER_DIED){
                mPlayer.release();
                init();
            }else{
                mPlayer.reset(); // 恢复
            }
            if(null != mCallback){
                mCallback.onError("播放器发生错误! 代码: " + what + "," + extra);
            }
            return false;
        }
    };

    public SongInfo getSource(){
        return mCurrent;
    }

    private void startThread() {
        if (playerThread == null) {
            isRunning = true;
            playerThread = new Thread(new PlayerRunnable());
            playerThread.start();
        }
    }

    private void stopThread() {
        boolean retry = true;
        isRunning = false;
        while (retry){
            try {
                if (playerThread != null) {
                    playerThread.join();
                    playerThread = null;
                }
                retry = false;
            }catch (Exception e){
            }
        }
    }

    private class PlayerRunnable implements Runnable {
        @Override
        public void run() {
            while (isRunning) {
                SystemClock.sleep(100);
                if (getState() == STATE_PLAYING && null != mCallback) {
                    long millis = getCurrentMillis(); // 毫秒
                    mCallback.onProgress(millis, 0); // 通知更新播放进度
                }
            }
        }
    }
}
