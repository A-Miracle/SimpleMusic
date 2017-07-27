package com.ctao.music.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.regex.Pattern;

/**
 * Created by A Miracle on 2017/6/28.
 * 歌曲实体类
 */
@DatabaseTable(tableName="SongInfo")
public class SongInfo implements Cloneable{
    @DatabaseField(id = true)
    private String id; // 歌曲id

    @DatabaseField(columnName = "displayName")
    private String displayName; // 歌曲文件名

    @DatabaseField(columnName = "name")
    private String name; // 歌曲名字

    @DatabaseField(columnName = "album")
    private String album; // 歌曲专辑

    @DatabaseField(columnName = "artist")
    private String artist; // 歌曲艺术家

    @DatabaseField(columnName = "genre")
    private String genre; // 歌曲类型

    @DatabaseField(columnName = "source")
    private String source; // 歌曲源

    @DatabaseField(columnName = "albumId")
    private String albumId; // 歌曲专辑id

    @DatabaseField(columnName = "albumUrl")
    private String albumUrl; // 歌曲专辑Icon

    @DatabaseField(columnName = "createTime")
    private String createTime; // 创建时间

    @DatabaseField(columnName = "durationMs")
    private long durationMs; // 歌曲时长 ms

    @DatabaseField(columnName = "size")
    private long size; // 歌曲文件大小

    @DatabaseField(columnName = "singerPIC")
    private String singerPIC;// 歌手写真图片下载路径

    @DatabaseField(columnName = "kscUrl")
    private String kscUrl;// 歌词文件下载路径

    @DatabaseField(columnName = "pinyin")
    private String pinyin; // 拼音首字母

    @DatabaseField(columnName = "folderPath")
    private String folderPath; // 所在文件夹路径

    public SongInfo() {
    }

    public SongInfo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public void setAlbumUrl(String albumUrl) {
        this.albumUrl = albumUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSingerPIC() {
        return singerPIC;
    }

    public void setSingerPIC(String singerPIC) {
        this.singerPIC = singerPIC;
    }

    public String getKscUrl() {
        return kscUrl;
    }

    public void setKscUrl(String kscUrl) {
        this.kscUrl = kscUrl;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        boolean matches = Pattern.compile("^[A-Z]+$").matcher(pinyin).matches();
        if(!matches){
            pinyin = "#";
        }
        this.pinyin = pinyin;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    @Override
    public SongInfo clone(){
        try {
            return (SongInfo)super.clone();
        } catch (CloneNotSupportedException e) {
            return new SongInfo();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongInfo songInfo = (SongInfo) o;
        return id.equals(songInfo.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "SongInfo{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", name='" + name + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", genre='" + genre + '\'' +
                ", source='" + source + '\'' +
                ", albumId='" + albumId + '\'' +
                ", albumUrl='" + albumUrl + '\'' +
                ", createTime='" + createTime + '\'' +
                ", durationMs=" + durationMs +
                ", size=" + size +
                ", singerPIC='" + singerPIC + '\'' +
                ", kscUrl='" + kscUrl + '\'' +
                ", pinyin='" + pinyin + '\'' +
                '}';
    }
}
