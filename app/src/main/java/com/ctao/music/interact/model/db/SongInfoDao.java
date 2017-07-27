package com.ctao.music.interact.model.db;

import com.ctao.baselib.utils.LogUtils;
import com.ctao.music.model.SongInfo;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by A Miracle on 2017/7/13.
 */
public class SongInfoDao {
    private Dao<SongInfo, Integer> dao;
    public SongInfoDao() {
        try {
            dao = DataBaseHelper.getInstance().getDao(SongInfo.class);
        } catch (SQLException e) {
            LogUtils.e(e);
        }
    }

    public int add(SongInfo info){
        try {
            return dao.create(info);
        } catch (SQLException e) {
            LogUtils.e(e);
        }
        return 0;
    }

    public int addAll(List<SongInfo> list){
        try {
            return dao.create(list);
        } catch (SQLException e) {
            LogUtils.e(e);
        }
        return 0;
    }

    public int update(SongInfo info){
        try {
            return dao.update(info);
        } catch (SQLException e) {
            LogUtils.e(e);
        }
        return 0;
    }

    public List<SongInfo> queryAll(){
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            LogUtils.e(e);
        }
        return new ArrayList<>();
    }

    public int deleteAll(){
        try {
            return dao.deleteBuilder().delete();
        } catch (SQLException e) {
            LogUtils.e(e);
        }
        return 0;
    }

    public int delete(SongInfo info){
        try {
            return dao.delete(info);
        } catch (SQLException e) {
            LogUtils.e(e);
        }
        return 0;
    }
}
