package com.ctao.music.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.callback.IPlayState;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.interact.contract.LrcContract;
import com.ctao.music.interact.contract.LrcPresenter;
import com.ctao.music.model.SongInfo;
import com.ctao.music.service.MediaProvider;
import com.ctao.music.ui.LrcActivity;
import com.ctao.music.ui.base.MvpFragment;
import com.ctao.music.ui.widget.LyricView;
import com.ctao.music.utils.LyricUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;

/**
 * Created by A Miracle on 2017/7/19.
 */
public class Lrc2Fragment extends MvpFragment implements LrcContract.View, SeekBar.OnSeekBarChangeListener {
    @BindView(R.id.iv_volume) ImageView iv_volume;
    @BindView(R.id.sb_volume) SeekBar sb_volume;
    @BindView(R.id.lrc_view) LyricView lrc_view;
    private LrcContract.Presenter mPresenter;
    private MediaProvider mProvider;
    private AudioManager mAudioManager;
    private String mAteKey;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_lrc2;
    }

    @Override
    protected void initView() {
        mAteKey = ((LrcActivity) getActivity()).getATEKey();
        mPresenter = new LrcPresenter(this);
        lrc_view.setLineSpace(15.0f);
        lrc_view.setOnPlayerClickListener(new LyricView.OnPlayerClickListener() {
            @Override
            public void onPlayerClicked(long progress, String content) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_SEEK_TO, progress));
                if(mProvider.getState() != IPlayState.STATE_PLAYING){
                    // 可以从暂停直接播放
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PLAY));
                }
            }
        });
        lrc_view.setPlayable(true);
        lrc_view.setTouchable(true);

        settingLrcView();

        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        sb_volume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sb_volume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        sb_volume.setOnSeekBarChangeListener(this);
        ATE.apply(iv_volume, mAteKey);
        ATE.apply(sb_volume, mAteKey);
        mProvider = new MediaProvider();
        SongInfo currentSong = mProvider.getCurrentSong();
        if(null != currentSong){
            loadLyric(currentSong);
        }
    }

    private void settingLrcView() {
        int hintColor = SPUtils.getInt(Constant.SP_LRC_HINT, 0);
        if(hintColor == 0){
            hintColor = Config.primaryColor(getContext(), mAteKey);
        }
        int highLightColor = SPUtils.getInt(Constant.SP_LRC_HIGH_LIGHT, 0);
        if(highLightColor == 0){
            highLightColor = Config.textColorPrimary(getContext(), mAteKey);
        }
        int defaultColor = SPUtils.getInt(Constant.SP_LRC_DEFAULT, 0);
        if(defaultColor == 0){
            defaultColor = Config.textColorSecondary(getContext(), mAteKey);
        }
        int indicatorColor = SPUtils.getInt(Constant.SP_LRC_INDICATOR, 0);
        if(indicatorColor == 0){
            indicatorColor = Config.primaryColorDark(getContext(), mAteKey);
        }
        int dragColor = SPUtils.getInt(Constant.SP_LRC_DRAG, 0);
        if(dragColor == 0){
            dragColor =  Color.parseColor("#AAAAAA"); // 这个默认的, 暂时没有对应
        }

        lrc_view.setHighLightTextColor(highLightColor);
        lrc_view.setDefaultColor(defaultColor);
        lrc_view.setHintColor(hintColor);
        lrc_view.setBtnColor(indicatorColor);
        lrc_view.setIndicatorColor(indicatorColor);
        lrc_view.setCurrentShowColor(dragColor);

        // 默认 17sp
        lrc_view.setTextSize(17 + SPUtils.getInt(Constant.SP_LRC_SIZE_INCREMENT, 0));
    }

    @Override
    public void showLyric(File file) {
        SongInfo song = mProvider.getCurrentSong();
        if(null != song && song.getId().equals(lrc_view.getTag())){
            if (file == null) {
                lrc_view.reset("暂无歌词");
            } else {
                lrc_view.setLyricFile(file, "UTF-8");
            }
        }
    }

    @Override
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()){
            case MessageEvent.MUSIC_UPDATE_PROGRESS: // 播放进度
                long[] param = (long[]) event.getParam();
                lrc_view.setCurrentTimeMillis(param[0]);
                break;
            case MessageEvent.MUSIC_CHANGE_SONG: // 更换播放歌曲
                SongInfo songInfo = (SongInfo) event.getParam();
                loadLyric(songInfo);
                break;
            case MessageEvent.MUSIC_LRC_SETTING:
                settingLrcView();
                break;
        }
    }

    private void loadLyric(SongInfo songInfo) {
        lrc_view.setTag(songInfo.getId()); // 防错乱
        String title = songInfo.getName();
        String album = songInfo.getAlbum();
        if(LyricUtils.isLrcFileExist(title, album)){
            File file = LyricUtils.getLocalLyricFile(title, album);
            showLyric(file);
        }else{
            mPresenter.downloadLrcFile(title, album, songInfo.getDurationMs());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(),
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    private BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sb_volume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");//AudioManager.VOLUME_CHANGED_ACTION
        getContext().registerReceiver(mVolumeReceiver, filter);
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mVolumeReceiver);
        super.onDestroy();
    }
}
