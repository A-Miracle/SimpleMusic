package com.ctao.music.utils;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.ctao.baselib.Global;
import com.ctao.baselib.utils.SystemUtils;
import com.ctao.music.callback.IContinue;
import com.ctao.music.callback.IResult;
import com.ctao.music.interact.model.db.SongInfoDao;
import com.ctao.music.manager.ThreadManager;
import com.ctao.music.model.SongInfo;
import com.ctao.music.ui.adpter.SongAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by A Miracle on 2017/6/28.
 */
public class MediaUtils {

    public static List<SongInfo> scanCallBack(IResult<Integer> count, IResult<SongInfo> progress, IContinue iContinue, IResult<List<SongInfo>> result){
        List<SongInfo> infos = new ArrayList<>();
        SongInfo songInfo = null;
        Cursor cursor = Global.getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if(null != cursor && cursor.moveToLast()){
            if(null != count){
                count.onResult(cursor.getPosition()); // 总条数
            }
        }

        if(null != cursor && cursor.moveToFirst()){

            int index_is_music = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC);
            int index_id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int index_title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int index_album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int index_artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int index_duration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int index_size = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int index_data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int index_album_id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int index_display_name = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int index_year = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);

            while (!cursor.isAfterLast()) {
                // 是否为音乐，魅族手机上始终为0
                int isMusic = cursor.getInt(index_is_music);
                if (!SystemUtils.isFlyme() && isMusic == 0) {
                    cursor.moveToNext();
                    continue;
                }

                int id = cursor.getInt(index_id);// 歌曲编号 _ID
                String title = cursor.getString(index_title); // 歌曲标题 TITLE
                String album = cursor.getString(index_album); // 歌曲的专辑名 ALBUM
                String artist = cursor.getString(index_artist); // 歌曲的歌手名 ARTIST
                int duration = cursor.getInt(index_duration); // 歌曲的总播放时长 DURATION
                long size = cursor.getLong(index_size); // 歌曲文件的大小 SIZE
                String url = cursor.getString(index_data); // 歌曲文件的路径 DATA
                long albumId = cursor.getLong(index_album_id); // 专辑封面ALBUM_ID，根据该id可以获得专辑图片uri
                String fileName = cursor.getString(index_display_name); // 音乐文件名 DISPLAY_NAME
                String year = cursor.getString(index_year); // 发行时间 YEAR

                if (size > 1024 * 800) { // 大于800K
                    if(null == songInfo){
                        songInfo = new SongInfo();
                    }else{
                        songInfo = songInfo.clone(); // 原型模式
                    }

                    if(TextUtils.isEmpty(title)){
                        int divider = fileName.indexOf("-"); // 宫崎骏 - 与你同在.mp3
                        int point = fileName.lastIndexOf(".");
                        if(divider != -1){
                            title = fileName.substring(divider + 2, point);
                        }else{
                            title = fileName.substring(0, divider);
                        }
                    }
                    songInfo.setId("" + id);
                    songInfo.setName(title);
                    songInfo.setAlbum(album);
                    songInfo.setArtist(artist);
                    songInfo.setSource(url);
                    songInfo.setDurationMs(duration);
                    songInfo.setSize(size);
                    songInfo.setPinyin(PingYinUtils.converterToFirstSpell(title.toCharArray()[0] + "").toUpperCase());
                    songInfo.setAlbumId("" + albumId);
                    songInfo.setDisplayName(fileName);
                    songInfo.setCreateTime(year);
                    songInfo.setFolderPath(url.substring(0, url.lastIndexOf(File.separator)));

                    infos.add(songInfo);
                    if(null != progress){
                        progress.onResult(songInfo);
                        SystemClock.sleep(16); //太快了, 没感觉, 哈哈
                    }
                }
                if(null != iContinue && !iContinue.isContinue()){ // 终止操作
                    cursor.close();
                    return infos;
                }

                cursor.moveToNext();
            }
            cursor.close();
        }

        SongInfoDao infoDao = new SongInfoDao();
        infoDao.deleteAll();
        infoDao.addAll(infos);
        if(null != result){
            result.onResult(infos);
        }
        return infos;
    }

    /**扫描SD卡上的歌曲文件*/
    public static List<SongInfo> scanSDCardSong(){
        return scanCallBack(null, null, null, null);
        /*List<SongInfo> infos = new ArrayList<>();
        SongInfo songInfo = null;
        Cursor cursor = Global.getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if(null != cursor && cursor.moveToFirst()){

            int index_is_music = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC);
            int index_id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int index_title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int index_album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int index_artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int index_duration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int index_size = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int index_data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int index_album_id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int index_display_name = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int index_year = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);

            while (!cursor.isAfterLast()) {
                // 是否为音乐，魅族手机上始终为0
                int isMusic = cursor.getInt(index_is_music);
                if (!SystemUtils.isFlyme() && isMusic == 0) {
                    cursor.moveToNext();
                    continue;
                }

                int id = cursor.getInt(index_id);// 歌曲编号 _ID
                String title = cursor.getString(index_title); // 歌曲标题 TITLE
                String album = cursor.getString(index_album); // 歌曲的专辑名 ALBUM
                String artist = cursor.getString(index_artist); // 歌曲的歌手名 ARTIST
                int duration = cursor.getInt(index_duration); // 歌曲的总播放时长 DURATION
                long size = cursor.getLong(index_size); // 歌曲文件的大小 SIZE
                String url = cursor.getString(index_data); // 歌曲文件的路径 DATA
                long albumId = cursor.getLong(index_album_id); // 专辑封面ALBUM_ID，根据该id可以获得专辑图片uri
                String coverUri = getCoverUri(albumId);
                String fileName = cursor.getString(index_display_name); // 音乐文件名 DISPLAY_NAME
                String year = cursor.getString(index_year); // 发行时间 YEAR

                if (size > 1024 * 800) { // 大于800K
                    if(null == songInfo){
                        songInfo = new SongInfo();
                    }else{
                        songInfo = songInfo.clone(); // 原型模式
                    }

                    songInfo.setId("" + id);
                    if(TextUtils.isEmpty(title)){
                        int divider = fileName.indexOf("-"); // 宫崎骏 - 与你同在.mp3
                        int point = fileName.lastIndexOf(".");
                        if(divider != -1){
                            title = fileName.substring(divider + 2, point);
                        }else{
                            title = fileName.substring(0, divider);
                        }
                    }
                    songInfo.setName(title);
                    songInfo.setAlbum(album);
                    songInfo.setArtist(artist);
                    songInfo.setSource(url);
                    songInfo.setDurationMs(duration);
                    songInfo.setSize(size);
                    songInfo.setPinyin(PingYinUtils.converterToFirstSpell(title.toCharArray()[0] + "").toUpperCase());
                    songInfo.setAlbumUrl(coverUri);
                    songInfo.setDisplayName(fileName);
                    songInfo.setCreateTime(year);
                    songInfo.setFolderPath(url.substring(0, url.lastIndexOf(File.separator)));

                    infos.add(songInfo);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }

        SongInfoDao infoDao = new SongInfoDao();
        infoDao.deleteAll();
        infoDao.addAll(infos);
        return infos;*/
    }

    /**直接异步扫描SD卡, 通过回调返回列表数据集合*/
    public static void getListSong(final IResult<List<SongAdapter.SongData>> result){
        if(null == result){
            return;
        }
        ThreadManager.getShortPool().execute(new Runnable() {
            @Override
            public void run() {
                List<SongInfo> infos = new SongInfoDao().queryAll();
                if(null == infos || infos.size() == 0){
                    infos = scanSDCardSong();
                }else{
                    System.out.println(">>>: "+infos.size());
                }
                final List<SongAdapter.SongData> datas = convertData(infos);
                Global.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        result.onResult(datas);
                    }
                });
            }
        });
    }

    /**将歌曲集合转换为列表数据集合*/
    public static List<SongAdapter.SongData> convertData(List<SongInfo> infos){
        List<SongAdapter.SongData> datas = new ArrayList<>();

        Collections.sort(infos, new Comparator<SongInfo>() { // 排序
            @Override
            public int compare(SongInfo o1, SongInfo o2) {
                String str1 = o1.getPinyin(), str2 = o2.getPinyin();
                if("#".equals(str1)){
                    str1 = "Z#";
                }
                if("#".equals(str2)){
                    str2 = "Z#";
                }
                return str1.compareTo(str2);
            }
        });

        String pinyin = "";
        for (SongInfo info : infos) {
            if(!pinyin.equals(info.getPinyin())){
                pinyin = info.getPinyin();
                datas.add(new SongAdapter.SongData(pinyin));
            }
            datas.add(new SongAdapter.SongData(info));
        }

        return datas;
    }

    /**将列表数据集合转换为歌曲集合*/
    public static List<SongInfo> reverseData(List<SongAdapter.SongData> datas){
        List<SongInfo> infos = new ArrayList<>();
        for (SongAdapter.SongData data : datas) {
            if(data.getType() == SongAdapter.TYPE_ITEM){
                infos.add(data.getSongInfo());
            }
        }
        return infos;
    }

    //______________________________________________________________________________________________
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    public static Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(albumArtUri, paramInt);
    }
}
