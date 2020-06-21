package com.ctao.music.ui.fragment;

import com.afollestad.appthemeengine.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.baselib.widget.RotateImageView;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.interact.contract.LrcContract;
import com.ctao.music.interact.contract.LrcPresenter;
import com.ctao.music.model.SongInfo;
import com.ctao.music.service.MediaProvider;
import com.ctao.music.ui.LrcActivity;
import com.ctao.music.ui.base.MvpFragment;
import com.ctao.music.ui.widget.LyricView;
import com.ctao.music.utils.LyricUtils;
import com.ctao.music.utils.MediaUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;

/**
 * Created by A Miracle on 2017/7/19.
 */
public class Lrc1Fragment extends MvpFragment implements LrcContract.View{
    @BindView(R.id.riv_icon) RotateImageView riv_icon;
    @BindView(R.id.lrc_view) LyricView lrc_view;
    private LrcContract.Presenter mPresenter;
    private MediaProvider mProvider;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_lrc1;
    }

    @Override
    protected void initView() {
        mPresenter = new LrcPresenter(this);
        lrc_view.setLineSpace(15.0f);
        lrc_view.setPlayable(false);
        lrc_view.setTouchable(false);
        lrc_view.setOnPlayerClickListener(new LyricView.OnPlayerClickListener() {
            @Override
            public void onPlayerClicked(long progress, String content) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_SEEK_TO, progress));
            }
        });

        settingLrcView();

        mProvider = new MediaProvider();
        SongInfo currentSong = mProvider.getCurrentSong();
        if(null != currentSong){
            loadLyric(currentSong);
            loadAlbum(currentSong);
        }
    }

    private void settingLrcView() {
        String ateKey = ((LrcActivity) getActivity()).getATEKey();

        int hintColor = SPUtils.getInt(Constant.SP_LRC_HINT, 0);
        if(hintColor == 0){
            hintColor = Config.primaryColor(getContext(), ateKey);
        }
        int highLightColor = SPUtils.getInt(Constant.SP_LRC_HIGH_LIGHT, 0);
        if(highLightColor == 0){
            highLightColor = Config.textColorPrimary(getContext(), ateKey);
        }
        int defaultColor = SPUtils.getInt(Constant.SP_LRC_DEFAULT, 0);
        if(defaultColor == 0){
            defaultColor = Config.textColorSecondary(getContext(), ateKey);
        }

        lrc_view.setHighLightTextColor(highLightColor);
        lrc_view.setDefaultColor(defaultColor);
        lrc_view.setHintColor(hintColor);

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
                loadAlbum(songInfo);
                riv_icon.setDegree(0); // 回正角度
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

    private void loadAlbum(SongInfo songInfo) {
        String uri = MediaUtils.getAlbumArtUri(Long.parseLong(songInfo.getAlbumId())).toString();
        Glide.with(getContext()).load(uri)
                .error(R.mipmap.ic_user)
                .placeholder(R.mipmap.ic_user)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .into(riv_icon);
    }
}
