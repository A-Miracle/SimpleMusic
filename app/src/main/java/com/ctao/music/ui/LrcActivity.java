package com.ctao.music.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.ctao.baselib.ui.BaseFragment;
import com.ctao.baselib.ui.adpter.FPagerAdapter;
import com.ctao.baselib.utils.DateUtils;
import com.ctao.baselib.utils.ResourcesUtils;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.baselib.utils.ToastUtils;
import com.ctao.baselib.widget.BorderCircleView;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.callback.IPlayPattern;
import com.ctao.music.callback.IPlayState;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.model.SongInfo;
import com.ctao.music.service.MediaProvider;
import com.ctao.music.ui.base.MvpActivity;
import com.ctao.music.ui.fragment.Lrc1Fragment;
import com.ctao.music.ui.fragment.Lrc2Fragment;
import com.ctao.music.ui.widget.AViewPager;
import com.ctao.music.ui.widget.IconTextView;
import com.ctao.music.ui.widget.IndicatorLayout;
import com.ctao.music.utils.ATEUtils;
import com.google.android.material.appbar.AppBarLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by A Miracle on 2017/6/27.
 */
public class LrcActivity extends MvpActivity implements SeekBar.OnSeekBarChangeListener, ViewPager.OnPageChangeListener, ColorChooserDialog.ColorCallback {

    @BindView(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.view_pager) AViewPager view_pager;
    @BindView(R.id.songProgress) TextView songProgress;
    @BindView(R.id.seekBar) SeekBar seekBar;
    @BindView(R.id.songSize) TextView songSize;

    @BindView(R.id.bt_pattern) IconTextView bt_pattern;
    @BindView(R.id.bt_pre) IconTextView bt_pre;
    @BindView(R.id.bt_play_pause)  IconTextView bt_play_pause;
    @BindView(R.id.bt_next) IconTextView bt_next;
    @BindView(R.id.bt_setting) IconTextView bt_setting;

    @BindView(R.id.il_indicator) IndicatorLayout il_indicator;

    private MediaProvider mMediaProvider;
    private boolean isCanChangeSeekBar = true; // 是否能手动更改SeekBar
    private long seekTo;
    private LrcSettingDialog mDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_lrc;
    }

    @Override
    protected Toolbar getBackToolBar() {
        return mToolbar;
    }

    @Override
    protected void onAfterSetContentLayout(Bundle savedInstanceState) {
        mAppBarLayout.setBackgroundColor(themeColor);
        seekBar.setMax(Integer.MAX_VALUE);
        mMediaProvider = new MediaProvider();

        SongInfo song = mMediaProvider.getCurrentSong();
        changeSong(song);

        int pattern = mMediaProvider.getPattern();
        changePattern(pattern);

        int state = mMediaProvider.getState();
        changePlayState(state);

        if(null != song){
            long currentMillis = mMediaProvider.getCurrentMillis();
            long durationMs = song.getDurationMs();
            seekBar.setProgress((int) (currentMillis * Integer.MAX_VALUE / durationMs));
            songProgress.setText(DateUtils.formatTime(currentMillis, "mm:ss"));
        }
        seekBar.setOnSeekBarChangeListener(this);

        FPagerAdapter adapter = new FPagerAdapter(getSupportFragmentManager());
        List<BaseFragment> fragments = adapter.getFragments();
        fragments.add(new Lrc1Fragment());
        fragments.add(new Lrc2Fragment());
        view_pager.setAdapter(adapter);
        view_pager.addOnPageChangeListener(this);

        il_indicator.create(fragments.size());
    }

    @OnClick({R.id.bt_pattern, R.id.bt_pre, R.id.bt_play_pause, R.id.bt_next, R.id.bt_setting})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.bt_pattern:
                int pattern = IPlayPattern.PATTERN_CYCLE;
                String patternText = "列表循环";
                switch (mMediaProvider.getPattern()) {
                    case IPlayPattern.PATTERN_CYCLE:
                        pattern = IPlayPattern.PATTERN_LIST;
                        patternText = "顺序播放";
                        break;
                    case IPlayPattern.PATTERN_LIST:
                        pattern = IPlayPattern.PATTERN_SINGLE;
                        patternText = "单曲循环";
                        break;
                    case IPlayPattern.PATTERN_SINGLE:
                        pattern = IPlayPattern.PATTERN_RANDOM;
                        patternText = "随机播放";
                        break;
                    case IPlayPattern.PATTERN_RANDOM:
                        pattern = IPlayPattern.PATTERN_CYCLE;
                        patternText = "列表循环";
                        break;
                }
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PATTERN, pattern));
                changePattern(pattern);
                ToastUtils.show(patternText);
                break;
            case R.id.bt_pre:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PRE));
                break;
            case R.id.bt_play_pause:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PLAY_OR_PAUSE));
                break;
            case R.id.bt_next:
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_NEXT));
                break;
            case R.id.bt_setting:
                int color = ResourcesUtils.getThemeColor(this, getActivityTheme(), android.R.attr.windowBackground, themeColor);
                mDialog = new LrcSettingDialog(this, color);
                mDialog.show();
                break;
        }
    }

    @Override
    public void onMessageEvent(MessageEvent event) {
        super.onMessageEvent(event);
        switch (event.getType()) {
            case MessageEvent.MUSIC_UPDATE_PROGRESS: // 播放进度
                long[] param = (long[]) event.getParam();
                if (isCanChangeSeekBar) {
                    seekBar.setProgress((int) (param[0] * Integer.MAX_VALUE / param[1]));
                    songProgress.setText(DateUtils.formatTime(param[0], "mm:ss"));
                }
                break;
            case MessageEvent.MUSIC_CHANGE_SONG: // 更换播放歌曲
                // icon_artist
                SongInfo songInfo = (SongInfo) event.getParam();
                changeSong(songInfo);
                break;
            case MessageEvent.MUSIC_CHANGE_STATE: // 改变播放状态
                int state = (int) event.getParam();
                changePlayState(state);
                break;
        }
    }

    private void changeSong(SongInfo song) {
        if (null != song) {
            mToolbar.setTitle(song.getName());
            mToolbar.setSubtitle(song.getArtist());
            songSize.setText(DateUtils.formatTime(song.getDurationMs(), "mm:ss"));
        } else {
            mToolbar.setTitle("可可音乐");
            mToolbar.setSubtitle("传播好音乐");
            songSize.setText("00:00");
        }
    }

    private void changePlayState(int state) {
        if (state != IPlayState.STATE_PLAYING) {
            bt_play_pause.setText(R.string.play_def_icon); // 播放图标
        } else {
            bt_play_pause.setText(R.string.pause_def_icon); // 暂停图标
        }
    }

    private void changePattern(int pattern) {
        switch (pattern) {
            case IPlayPattern.PATTERN_CYCLE:
                bt_pattern.setText(R.string.mode_cycle_def_icon);
                break;
            case IPlayPattern.PATTERN_LIST:
                bt_pattern.setText(R.string.mode_list_def_icon);
                break;
            case IPlayPattern.PATTERN_SINGLE:
                bt_pattern.setText(R.string.mode_single_def_icon);
                break;
            case IPlayPattern.PATTERN_RANDOM:
                bt_pattern.setText(R.string.mode_random_def_icon);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        seekTo = progress * mMediaProvider.getCurrentSong().getDurationMs() / seekBar.getMax();
        songProgress.setText(DateUtils.formatTime(seekTo, "mm:ss"));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isCanChangeSeekBar = false;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isCanChangeSeekBar = true;
        EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_SEEK_TO, seekTo));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        il_indicator.setCurrent(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        switch (dialog.getTitle()) {
                case R.string.lrc_high_light:
                    SPUtils.putObject(Constant.SP_LRC_HIGH_LIGHT, selectedColor);
                    break;
                case R.string.lrc_default:
                    SPUtils.putObject(Constant.SP_LRC_DEFAULT, selectedColor);
                    break;
                case R.string.lrc_hint:
                    SPUtils.putObject(Constant.SP_LRC_HINT, selectedColor);
                    break;
                case R.string.lrc_indicator:
                    SPUtils.putObject(Constant.SP_LRC_INDICATOR, selectedColor);
                    break;
                case R.string.lrc_drag:
                    SPUtils.putObject(Constant.SP_LRC_DRAG, selectedColor);
                    break;
            }
            if(null != mDialog && mDialog.isShowing()){
                mDialog.refreshView();
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_LRC_SETTING));
        }
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    class LrcSettingDialog extends AlertDialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
        private int sizeMax = 10;
        private int backgroundColor;
        private String mAteKey;

        private BorderCircleView bcv_high_light;
        private BorderCircleView bcv_default;
        private BorderCircleView bcv_hint;
        private BorderCircleView bcv_indicator;
        private BorderCircleView bcv_drag;
        private SeekBar sb_size;

        protected LrcSettingDialog(@NonNull Context context, int backgroundColor) {
            super(context, R.style.dialogStyle);
            this.backgroundColor = backgroundColor;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //获得dialog的window窗口
            Window window = getWindow();
            //设置dialog在屏幕底部
            window.setGravity(Gravity.BOTTOM);
            //设置dialog弹出时的动画效果，从屏幕底部向上弹出
            window.setWindowAnimations(R.style.dialogAnimationStyle);
            window.getDecorView().setPadding(0, 0, 0, 0);
            //获得window窗口的属性
            WindowManager.LayoutParams lp = window.getAttributes();
            //设置窗口宽度为充满全屏
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            //设置窗口高度为包裹内容
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            //将设置好的属性set回去
            window.setAttributes(lp);
            setCanceledOnTouchOutside(true);//设置点击Dialog外部任意区域关闭Dialog

            View contentView = View.inflate(getContext(), R.layout.dialog_lrc_setting, null);
            View ll_root = contentView.findViewById(R.id.ll_root);
            ll_root.setBackgroundColor(backgroundColor);
            ll_root.setAlpha(0.86f); //半透明
            setContentView(contentView);
            mAteKey = ATEUtils.getATEKey();
            ATE.apply(contentView, mAteKey);
            initView();
            refreshView();
        }

        private void initView() {
            bcv_high_light = (BorderCircleView) findViewById(R.id.bcv_high_light);
            bcv_default = (BorderCircleView) findViewById(R.id.bcv_default);
            bcv_hint = (BorderCircleView) findViewById(R.id.bcv_hint);
            bcv_indicator = (BorderCircleView) findViewById(R.id.bcv_indicator);
            bcv_drag = (BorderCircleView) findViewById(R.id.bcv_drag);
            sb_size = (SeekBar) findViewById(R.id.sb_size);
            sb_size.setMax(sizeMax);

            findViewById(R.id.ml_high_light).setOnClickListener(this);
            findViewById(R.id.ml_default).setOnClickListener(this);
            findViewById(R.id.ml_hint).setOnClickListener(this);
            findViewById(R.id.ml_indicator).setOnClickListener(this);
            findViewById(R.id.ml_drag).setOnClickListener(this);
            findViewById(R.id.ml_restore).setOnClickListener(this);

            findViewById(R.id.tv_decrease).setOnClickListener(this);
            findViewById(R.id.tv_increase).setOnClickListener(this);
            sb_size.setOnSeekBarChangeListener(this);
        }

        public void refreshView(){
            int preselect = SPUtils.getInt(Constant.SP_LRC_HIGH_LIGHT, 0);
            if(preselect == 0){
                preselect = Config.textColorPrimary(getContext(), mAteKey);
            }
            bcv_high_light.setBackgroundColor(preselect);

            preselect = SPUtils.getInt(Constant.SP_LRC_DEFAULT, 0);
            if(preselect == 0){
                preselect = Config.textColorSecondary(getContext(), mAteKey);
            }
            bcv_default.setBackgroundColor(preselect);

            preselect = SPUtils.getInt(Constant.SP_LRC_HINT, 0);
            if(preselect == 0){
                preselect = Config.primaryColor(getContext(), mAteKey);
            }
            bcv_hint.setBackgroundColor(preselect);

            preselect = SPUtils.getInt(Constant.SP_LRC_INDICATOR, 0);
            if(preselect == 0){
                preselect = Config.primaryColorDark(getContext(), mAteKey);
            }
            bcv_indicator.setBackgroundColor(preselect);

            preselect = SPUtils.getInt(Constant.SP_LRC_DRAG, 0);
            if(preselect == 0){
                preselect = Color.parseColor("#AAAAAA"); // 这个默认的, 暂时没有对应
            }
            bcv_drag.setBackgroundColor(preselect);

            sb_size.setProgress(SPUtils.getInt(Constant.SP_LRC_SIZE_INCREMENT, 0));
        }

        @Override
        public void onClick(View v) {
            int preselect = 0;
            int title = 0;
            switch (v.getId()){
                case R.id.ml_high_light:
                    title = R.string.lrc_high_light;
                    preselect = SPUtils.getInt(Constant.SP_LRC_HIGH_LIGHT, 0);
                    if(preselect == 0){
                        preselect = Config.textColorPrimary(getContext(), mAteKey);
                    }
                    break;
                case R.id.ml_default:
                    title = R.string.lrc_default;
                    preselect = SPUtils.getInt(Constant.SP_LRC_DEFAULT, 0);
                    if(preselect == 0){
                        preselect = Config.textColorSecondary(getContext(), mAteKey);
                    }
                    break;
                case R.id.ml_hint:
                    title = R.string.lrc_hint;
                    preselect = SPUtils.getInt(Constant.SP_LRC_HINT, 0);
                    if(preselect == 0){
                        preselect = Config.primaryColor(getContext(), mAteKey);
                    }
                    break;
                case R.id.ml_indicator:
                    title = R.string.lrc_indicator;
                    preselect = SPUtils.getInt(Constant.SP_LRC_INDICATOR, 0);
                    if(preselect == 0){
                        preselect = Config.primaryColorDark(getContext(), mAteKey);
                    }
                    break;
                case R.id.ml_drag:
                    title = R.string.lrc_drag;
                    preselect = SPUtils.getInt(Constant.SP_LRC_DRAG, 0);
                    if(preselect == 0){
                        preselect = Color.parseColor("#AAAAAA"); // 这个默认的, 暂时没有对应
                    }
                    break;
                case R.id.ml_restore:
                    restore();
                    break;
                case R.id.tv_decrease:// A-
                    changeSize(-1);
                    break;
                case R.id.tv_increase:// A+
                    changeSize(1);
                    break;
            }

            if(title != 0){
                getDialogBuilder(title).preselect(preselect).show(LrcActivity.this);
            }
        }

        private void changeSize(int change) {
            int size = SPUtils.getInt(Constant.SP_LRC_SIZE_INCREMENT, 0);
            if(change > 0 && ++size <= sizeMax){
                sb_size.setProgress(size);
                SPUtils.putObject(Constant.SP_LRC_SIZE_INCREMENT, size);
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_LRC_SETTING));
            }else if(change < 0 && --size >= 0){
                sb_size.setProgress(size);
                SPUtils.putObject(Constant.SP_LRC_SIZE_INCREMENT, size);
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_LRC_SETTING));
            }
        }

        private void restore() {
            new MaterialDialog.Builder(LrcActivity.this)
                    .title("恢复默认")
                    .content("恢复默认, 清除自定义设置")
                    .negativeText("取消")
                    .positiveText("确认")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            switch (which) {
                                case POSITIVE: // 恢复默认
                                    SPUtils.builder()
                                            .putInt(Constant.SP_LRC_HIGH_LIGHT, 0)
                                            .putInt(Constant.SP_LRC_DEFAULT, 0)
                                            .putInt(Constant.SP_LRC_HINT, 0)
                                            .putInt(Constant.SP_LRC_INDICATOR, 0)
                                            .putInt(Constant.SP_LRC_DRAG, 0)
                                            .putInt(Constant.SP_LRC_SIZE_INCREMENT, 0)
                                            .commit();
                                    refreshView();
                                    EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_LRC_SETTING));
                                    break;
                            }
                        }
                    })
                    .show();
        }

        public ColorChooserDialog.Builder getDialogBuilder(int title){
            return new ColorChooserDialog.Builder(LrcActivity.this, title)
                    .doneButton(R.string.color_done)
                    .cancelButton(R.string.color_cancel)
                    .backButton(R.string.color_back)
                    .customButton(R.string.color_custom)
                    .presetsButton(R.string.color_presets);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            SPUtils.putObject(Constant.SP_LRC_SIZE_INCREMENT, seekBar.getProgress());
            EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_LRC_SETTING));
        }
    }
}
