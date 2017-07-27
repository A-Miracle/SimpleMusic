package com.ctao.music.ui.fragment;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.ctao.baselib.ui.adpter.RBaseAdapter;
import com.ctao.baselib.ui.adpter.holder.RViewHolder;
import com.ctao.baselib.utils.SPUtils;
import com.ctao.baselib.utils.ToastUtils;
import com.ctao.music.Constant;
import com.ctao.music.R;
import com.ctao.music.callback.IResult;
import com.ctao.music.event.MessageEvent;
import com.ctao.music.model.SongInfo;
import com.ctao.music.ui.adpter.SongAdapter;
import com.ctao.music.ui.base.MvpFragment;
import com.ctao.music.ui.widget.SideBar;
import com.ctao.music.ui.widget.SongRecyclerView;
import com.ctao.music.utils.ATEUtils;
import com.ctao.music.utils.MediaUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by A Miracle on 2017/6/25.
 * MainActivity的中心内容
 */
public final class ContainerFragment extends MvpFragment implements SideBar.OnTouchingLetterChangedListener, RBaseAdapter.OnItemClickListener, SongRecyclerView.OnTitleChangedListener {
    public static final int MSG_POSITION = 0xA;

    @BindView(R.id.tv_dialog) TextView tv_dialog;
    @BindView(R.id.sidebar) SideBar sidebar;
    @BindView(R.id.recyclerView) SongRecyclerView recyclerView;
    @BindView(R.id.progress_bar) ProgressBar progress_bar;
    @BindView(R.id.fab_position) FloatingActionButton fab_position;

    private SongAdapter mAdapter;
    private List<SongAdapter.SongData> mData;
    private SongAdapter.SongData mCurrent;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_POSITION:
                    if(null != fab_position){ // 由于延迟3秒
                        fab_position.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_container;
    }

    @Override
    protected void initView() {
        sidebar.setTextView(tv_dialog);
        sidebar.setOnTouchingLetterChangedListener(this);

        recyclerView.setOnTitleChangedListener(this);

        mAdapter = new SongAdapter();
        mAdapter.setOnItemClickListener(this);

        mData = mAdapter.getData();

        boolean flag = SPUtils.getBoolean(Constant.SP_INITIALIZE, false);
        if(!flag){
            ToastUtils.show("正在加载初始化数据, 请稍等片刻...");
        }
        showProgress();
        MediaUtils.getListSong(new IResult<List<SongAdapter.SongData>>() {
            @Override
            public void onResult(List<SongAdapter.SongData> datas) {
                hideProgress();
                mData.addAll(datas);
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_LIST_INIT, MediaUtils.reverseData(datas)));
                recyclerView.setAdapter(mAdapter);
                SPUtils.putObject(Constant.SP_INITIALIZE, true);
            }
        });

        recyclerView.addOnScrollListener(mOnScrollListener);

        String ateKey = ATEUtils.getATEKey();
        if(Color.alpha(Config.primaryColor(getContext(), ateKey)) >= 51){
            fab_position.setTag("tint_primary_color");
            ATE.apply(fab_position, ateKey);
        }else if(Color.alpha(Config.accentColor(getContext(), ateKey)) >= 51){
            fab_position.setTag("tint_accent_color");
            ATE.apply(fab_position, ateKey);
        }
    }

    @OnClick(R.id.fab_position)
    public void position(){
        if(null != mCurrent){
            int index = mData.indexOf(mCurrent);
            recyclerView.moveToPosition(index);
        }
    }

    @Override
    public void showProgress() {
        progress_bar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progress_bar.setVisibility(View.GONE);
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int index = 0;
        for (SongAdapter.SongData data : mData) {
            if(s.equals(data.getIndex())){
                recyclerView.smoothMoveToPosition(index);
                return;
            }
            index++;
        }
    }

    @Override
    public void OnTitleChangedListener(String s) {
        sidebar.setChoose(s);
    }

    long[] mHits = new long[2];
    @Override
    public void onItemClick(RecyclerView.Adapter<RViewHolder> parent, RViewHolder holder, View view, int position) {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);//数组的值向左移一位
        mHits[mHits.length-1] = SystemClock.uptimeMillis();//距离开机的时间
        if(mHits[1] - mHits[0] <= 500){
           ToastUtils.show("客官您太急了, 请慢慢来");
            return;
        }
        SongAdapter.SongData songData = mData.get(position);
        if(songData.getType() == SongAdapter.TYPE_ITEM){
            EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_PLAY_OR_PAUSE, songData.getSongInfo()));
        }
    }

    @Override
    public void onMessageEvent(MessageEvent event) {
        super.onMessageEvent(event);
        switch (event.getType()){
            case MessageEvent.MUSIC_CHANGE_SONG: // 更换播放歌曲
                SongInfo songInfo = (SongInfo) event.getParam();
                mAdapter.setPlaySongId(songInfo.getId());
                if(null == mCurrent){
                    mCurrent = new SongAdapter.SongData(songInfo);
                }else{
                    mCurrent.setSongInfo(songInfo);
                }
                break;
            case MessageEvent.MUSIC_SCAN:
                List<SongInfo> infos = (List<SongInfo>) event.getParam();
                if(null != infos){
                    List<SongAdapter.SongData> datas = MediaUtils.convertData(infos);
                    mData.clear();
                    mData.addAll(datas);
                    mAdapter.notifyDataSetChanged();
                }
                EventBus.getDefault().post(new MessageEvent(MessageEvent.MUSIC_LIST_INIT, infos));
                break;
        }
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            showFabPosition();
            if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                hideFabPosition();
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                hideFabPosition();
            }
        }
    };

    private void showFabPosition(){
        if(fab_position.getVisibility() == View.VISIBLE){
            return;
        }
        if(null != mCurrent && mData.contains(mCurrent)){
            fab_position.setVisibility(View.VISIBLE);
        }
    }

    private void hideFabPosition(){
        // 延迟3秒
        mHandler.removeMessages(MSG_POSITION);
        mHandler.sendEmptyMessageDelayed(MSG_POSITION, 3000);
    }

    @Override
    public void onDestroyView() {
        if(null != mHandler){
            mHandler.removeMessages(MSG_POSITION);
        }
        super.onDestroyView();
    }
}
