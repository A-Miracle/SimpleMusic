package com.ctao.music.callback;

import android.content.Context;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

/**
 * Created by A Miracle on 2017/6/30.
 */
class MediaStatePlayer extends MediaPlayer implements IPlayState{
    private int mState = STATE_IDLE;
    private long mOffset; // seekTo偏移, 由于prepareAsync异步

    @Override
    public void reset() {
        super.reset();
        mState = STATE_IDLE;
    }

    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri, @Nullable Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        super.setDataSource(context, uri, headers);
        mState = STATE_INITIALIZED;
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        super.setDataSource(path);
        mState = STATE_INITIALIZED;
    }

    @Override
    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        super.setDataSource(fd, offset, length);
        mState = STATE_INITIALIZED;
    }

    @Override
    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException, IllegalStateException {
        super.setDataSource(dataSource);
        mState = STATE_INITIALIZED;
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        super.prepareAsync();
        mState = STATE_PREPARING;
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        super.prepare();
        mState = STATE_PREPARED;
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        mState = STATE_PLAYING;
        mOffset = 0;
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        mState = STATE_PAUSED;
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        mState = STATE_STOPPED;
    }

    @Override
    public void release() {
        super.release();
        mState = STATE_END;
    }

    @Override
    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    public void setOffset(long ms){
        mOffset = ms;
    }

    public long getOffset(){
       return mOffset;
    }
}
