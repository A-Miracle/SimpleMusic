package com.ctao.music.ui.adpter;

import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.Config;
import com.ctao.baselib.Global;
import com.ctao.baselib.ui.adpter.RBaseAdapter;
import com.ctao.baselib.ui.adpter.holder.RViewHolder;
import com.ctao.music.R;
import com.ctao.music.model.SongInfo;
import com.ctao.music.utils.ATEUtils;

/**
 * Created by A Miracle on 2017/6/27.
 */
public final class SongAdapter extends RBaseAdapter<SongAdapter.SongData>{
    public final static int TYPE_TITLE = 0;
    public final static int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    private String selectedId; // 当前选中播放歌曲
    private String mAteKey;
    public SongAdapter(){
        mAteKey = ATEUtils.getATEKey();
    }

    public void setPlaySongId(String songId){
        selectedId = songId;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1; // TYPE_FOOTER类型
    }

    @Override
    public int getItemViewType(int position) {
        if(position == getItemCount() - 1){
            return TYPE_FOOTER;
        }
        return mList.get(position).getType();
    }

    @Override
    public RViewHolder onCreateRViewHolder(ViewGroup parent, int viewType) {
        RViewHolder holder = null;
        int textColorPrimary = Config.textColorPrimary(Global.getContext(), mAteKey);
        int textColorSecondary = Config.textColorSecondary(Global.getContext(), mAteKey);
        switch (viewType){
            case TYPE_TITLE:
                holder = new RViewHolder(R.layout.item_music_title, parent);
                holder.setTextColor(R.id.tv_letter, textColorSecondary);
                holder.setBackgroundColor(R.id.view_line, textColorSecondary);
                break;
            case TYPE_ITEM:
                holder = new RViewHolder(R.layout.item_music, parent);
                holder.setBackgroundColor(R.id.view_status, textColorSecondary);
                holder.setTextColor(R.id.tv_songName, textColorPrimary);
                holder.setTextColor(R.id.tv_artist, textColorSecondary);
                holder.setBackgroundColor(R.id.view_line, textColorSecondary);
                break;
            case TYPE_FOOTER:
                holder =  new RViewHolder(R.layout.item_music_foot, parent);
                holder.setTextColor(R.id.tv_count, textColorSecondary);
                break;
        }
        return holder;
    }

    @Override
    public void onBindRViewHolder(RViewHolder holder, int position, int viewType) {
        switch (viewType){
            case TYPE_TITLE:
                holder.setText(R.id.tv_letter, mList.get(position).getIndex());
                break;
            case TYPE_ITEM:
                SongInfo songInfo = mList.get(position).getSongInfo();
                holder.setText(R.id.tv_songName, songInfo.getName());
                holder.setText(R.id.tv_artist, songInfo.getArtist());
                if(songInfo.getId().equals(selectedId)){
                    holder.setVisibility(R.id.view_status, View.VISIBLE);
                }else{
                    holder.setVisibility(R.id.view_status, View.INVISIBLE);
                }
                break;
            case TYPE_FOOTER:
                int count = 0;
                for (SongData data : mList) {
                    if(data.getType() == TYPE_ITEM){
                        count++;
                    }
                }
                holder.setText(R.id.tv_count, "共有" + count + "首歌曲");
                break;
        }
    }


    public static class SongData {
        private int type;
        private String index;
        private SongInfo songInfo;

        public SongData(){}

        public SongData(String index) {
            this.type = TYPE_TITLE;
            this.index = index;
        }

        public SongData(SongInfo songInfo) {
            this.type = TYPE_ITEM;
            this.songInfo = songInfo;
        }

        public int getType() {
            return type;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.type = TYPE_TITLE;
            this.index = index;
        }

        public SongInfo getSongInfo() {
            return songInfo;
        }

        public void setSongInfo(SongInfo songInfo) {
            this.type = TYPE_ITEM;
            this.songInfo = songInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SongData songData = (SongData) o;

            if (type != songData.type) return false;
            if (index != null ? !index.equals(songData.index) : songData.index != null)
                return false;
            return songInfo != null ? songInfo.equals(songData.songInfo) : songData.songInfo == null;
        }

        @Override
        public int hashCode() {
            int result = type;
            result = 31 * result + (index != null ? index.hashCode() : 0);
            result = 31 * result + (songInfo != null ? songInfo.hashCode() : 0);
            return result;
        }
    }
}
