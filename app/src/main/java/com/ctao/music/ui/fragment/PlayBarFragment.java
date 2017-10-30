package com.ctao.music.ui.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ctao.music.R;
import com.ctao.music.callback.IPlayState;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.model.SongInfo;
import com.ctao.music.service.MediaProvider;
import com.ctao.music.ui.LrcActivity;
import com.ctao.music.ui.base.MvpFragment;
import com.ctao.music.ui.widget.IconTextView;
import com.ctao.music.utils.MediaUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by A Miracle on 2017/6/25.
 * MainActivity的中心内容
 */
public final class PlayBarFragment extends MvpFragment {

    @BindView(R.id.icon_artist) ImageView icon_artist;
    @BindView(R.id.tv_songName) TextView tv_songName;
    @BindView(R.id.tv_artistName) TextView tv_artistName;
    @BindView(R.id.bt_pre) IconTextView bt_pre;
    @BindView(R.id.bt_play_pause) IconTextView bt_play_pause;
    @BindView(R.id.bt_next) IconTextView bt_next;
    @BindView(R.id.seekBar) SeekBar seekBar;

    @BindString(R.string.bar_play_icon) String bar_play_icon;
    @BindString(R.string.bar_pause_icon) String bar_pause_icon;

    private MediaProvider mMediaProvider;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_play_bar;
    }

    @Override
    protected void initView() {
        seekBar.setMax(Integer.MAX_VALUE);
        seekBar.post(new Runnable() {
            @Override
            public void run() {
                // 很奇怪[魅族note5], xml中设置无效
                if(seekBar != null){
                    seekBar.setPadding(0, 0, 0, 0);
                }
            }
        });
        mMediaProvider = new MediaProvider();
    }

    @Override
    public void onResume() {
        super.onResume();
        SongInfo song = mMediaProvider.getCurrentSong();
        if(null != song){
            long currentMillis = mMediaProvider.getCurrentMillis();
            long durationMs = song.getDurationMs();
            seekBar.setProgress((int) (currentMillis * Integer.MAX_VALUE / durationMs));
        }
    }

    @OnClick(R.id.rootView)
    public void openLyrics(){
        startActivity(new Intent(getContext(), LrcActivity.class));
    }

    @OnClick({R.id.bt_pre, R.id.bt_play_pause, R.id.bt_next})
    public void click(View view) {
        switch (view.getId()){
            case R.id.bt_pre:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PRE));
                break;
            case R.id.bt_play_pause:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PLAY_OR_PAUSE));
                break;
            case R.id.bt_next:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_NEXT));
                break;
        }
    }

    @Override
    public void onMessageEvent(MessageEvent event) {
        super.onMessageEvent(event);
        switch (event.getType()){
            case MessageEvent.MUSIC_UPDATE_PROGRESS: // 播放进度
                long[] param = (long[]) event.getParam();
                seekBar.setProgress((int) (param[0] * Integer.MAX_VALUE / param[1]));
                break;
            case MessageEvent.MUSIC_CHANGE_SONG: // 更换播放歌曲
                // icon_artist
                SongInfo songInfo = (SongInfo) event.getParam();
                tv_songName.setText(songInfo.getName());
                tv_artistName.setText(songInfo.getArtist());

                String uri = MediaUtils.getAlbumArtUri(Long.parseLong(songInfo.getAlbumId())).toString();
                Glide.with(getContext()).load(uri)
                        .error(R.mipmap.ic_user)
                        .placeholder(R.mipmap.ic_user)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .centerCrop()
                        .into(icon_artist);
                break;
            case MessageEvent.MUSIC_CHANGE_STATE: // 改变播放状态
                int state = (int) event.getParam();
                if(state != IPlayState.STATE_PLAYING){
                    bt_play_pause.setText(bar_play_icon); // 播放图标
                }else{
                    bt_play_pause.setText(bar_pause_icon); // 暂停图标
                }
                break;
        }

    }
}
