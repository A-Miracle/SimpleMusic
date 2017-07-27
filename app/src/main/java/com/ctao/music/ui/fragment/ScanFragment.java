package com.ctao.music.ui.fragment;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.ctao.baselib.Global;
import com.ctao.music.R;
import com.ctao.music.callback.IContinue;
import com.ctao.music.callback.IResult;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.manager.ThreadManager;
import com.ctao.music.model.SongInfo;
import com.ctao.music.ui.common.CommonActivity;
import com.ctao.music.ui.common.CommonFragment;
import com.ctao.music.ui.widget.ScanView;
import com.ctao.music.utils.MediaUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by A Miracle on 2017/7/25.
 */
public class ScanFragment extends CommonFragment{
    private final static int STATE_NONE = 0xA;
    private final static int STATE_ONGOING = 0xB;
    private final static int STATE_COMPLETE = 0xC;
    private final static int STATE_CANCEL = 0xD;

    @BindView(R.id.ll_root) LinearLayout ll_root;
    @BindView(R.id.view_scan) ScanView view_scan;
    @BindView(R.id.tv_progress) TextView tv_progress;
    @BindView(R.id.tv_hint) TextView tv_hint;
    @BindView(R.id.bt_scan) Button bt_scan;

    private int count;
    private int current;
    private boolean isContinue = true;
    private List<SongInfo> songInfos;
    private int state = STATE_NONE;

    @Override
    public String getTitle() {
        return Global.getContext().getString(R.string.label_scan);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_scan;
    }

    @Override
    protected void initView() {
        ATE.apply(ll_root, ((CommonActivity)getActivity()).getATEKey());
    }

    @OnClick(R.id.bt_scan)
    public void scan(){
        switch (state){
            case STATE_NONE: // 未开始点击 -> 开始扫描
            case STATE_CANCEL: // 取消 -> 可选择重新扫描
                state = STATE_ONGOING;
                view_scan.start();
                bt_scan.setText("取消");
                count = current =0;
                isContinue = true;
                scannerSong();
                break;
            case STATE_ONGOING:
                // 进行中点击 -> 取消
                state = STATE_CANCEL;
                view_scan.stop();
                bt_scan.setText("重新扫描");
                isContinue = false;
                break;
            case STATE_COMPLETE:
                // 完成 -> 进行完成的操作
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_SCAN, songInfos));
                getActivity().finish();
                break;
        }
    }

    private void scannerSong() {
        ThreadManager.getShortPool().execute(new Runnable() {
            @Override
            public void run() {
                MediaUtils.scanCallBack(new IResult<Integer>() {
                    @Override
                    public void onResult(Integer integer) {
                        count = integer;
                    }
                }, new IResult<SongInfo>() {
                    @Override
                    public void onResult(final SongInfo info) {
                        current++;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(count != 0){
                                    tv_progress.setText(current * 100 / count + "%");
                                }
                                tv_hint.setText(info.getSource());
                            }
                        });
                    }
                }, new IContinue() {
                    @Override
                    public boolean isContinue() {
                        return isContinue;
                    }
                }, new IResult<List<SongInfo>>() {
                    @Override
                    public void onResult(List<SongInfo> infos) {
                        songInfos = infos;
                        state = STATE_COMPLETE;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_progress.setText("100%");
                                tv_hint.setText("扫描完成, 共扫描到" + current + "首歌曲");
                                bt_scan.setText("添加到我的音乐");
                                view_scan.stop();
                            }
                        });
                    }
                });
            }
        });
    }
}
