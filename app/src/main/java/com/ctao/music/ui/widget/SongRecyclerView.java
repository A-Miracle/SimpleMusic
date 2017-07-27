package com.ctao.music.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ctao.music.ui.adpter.SongAdapter;

import java.util.List;

/**
 * Created by A Miracle on 2017/6/28.
 */
public final class SongRecyclerView extends RecyclerView {
    private LinearLayoutManager layoutManager;
    private OnTitleChangedListener mListener;
    private String mLetter = "";

    public SongRecyclerView(Context context) {
        this(context, null);
    }

    public SongRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SongRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        layoutManager = new LinearLayoutManager(getContext());
        setHasFixedSize(true);
        setLayoutManager(layoutManager);
        addOnScrollListener(mOnScrollListener);
    }

    public void setOnTitleChangedListener(OnTitleChangedListener listener){
        mListener = listener;
    }

    /**当前显示字母*/
    public void setTitle(String letter){
        mLetter = letter;
    }

    private boolean mAutoScrolling; //由SideBar触发的自动滚动
    private boolean mShouldScroll;
    private int mToPosition;
    public void smoothMoveToPosition(final int position) {
        mAutoScrolling = true;
        int firstItem = layoutManager.findFirstVisibleItemPosition();
        int lastItem = layoutManager.findLastVisibleItemPosition();

        if (position <= firstItem ) {
            // 如果要跳转的位置在第一个可见项之前，则smoothScrollToPosition可以直接跳转
            smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // 如果要跳转的位置在第一个可见项之后，且在最后一个可见项之前
            // smoothScrollToPosition根本不会动，此时调用smoothScrollBy来滑动到指定位置
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < getChildCount()) {
                int top = getChildAt(movePosition).getTop();
                smoothScrollBy(0, top);
            }
        } else {
            // 如果要跳转的位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，进入上一个控制语句
            smoothScrollToPosition(position);
            mShouldScroll = true;
            mToPosition = position;
        }
    }

    private boolean mScroll;
    private int mPosition;
    public void moveToPosition(final int position) {
        int firstItem = layoutManager.findFirstVisibleItemPosition();
        int lastItem = layoutManager.findLastVisibleItemPosition();

        if (position <= firstItem ) {
            // 如果要跳转的位置在第一个可见项之前，则smoothScrollToPosition可以直接跳转
            scrollToPosition(position);
        } else if (position <= lastItem) {
            // 如果要跳转的位置在第一个可见项之后，且在最后一个可见项之前
            // smoothScrollToPosition根本不会动，此时调用smoothScrollBy来滑动到指定位置
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < getChildCount()) {
                int top = getChildAt(movePosition).getTop();
                scrollBy(0, top);
            }
        } else {
            // 如果要跳转的位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，进入上一个控制语句
            scrollToPosition(position);
            mScroll = true;
            mPosition = position;
        }
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(!mAutoScrolling){ // 当不是右侧SideBar触发滚动时
                int firstItem = layoutManager.findFirstVisibleItemPosition();
                Adapter adapter = getAdapter();
                if(null != adapter && adapter instanceof SongAdapter){
                    SongAdapter songAdapter = (SongAdapter) adapter;
                    List<SongAdapter.SongData> data = songAdapter.getData();
                    if(null == data || data.size() == 0){
                        return;
                    }
                    SongAdapter.SongData songData = data.get(firstItem);
                    String pinyin;
                    if(songData.getType() == SongAdapter.TYPE_TITLE){
                        pinyin = songData.getIndex();
                    }else{
                        pinyin = songData.getSongInfo().getPinyin();
                    }
                    if(!pinyin.equals(mLetter) && mListener != null){
                        mLetter = pinyin;
                        mListener.OnTitleChangedListener(pinyin);
                    }
                }
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                 mAutoScrolling = false;
                if (mShouldScroll) {
                    mShouldScroll = false;
                    smoothMoveToPosition(mToPosition);
                }
                if(mScroll){
                    mScroll = false;
                    moveToPosition(mPosition);
                }
            }
        }
    };


    public interface OnTitleChangedListener {
        void OnTitleChangedListener(String s);
    }
}
