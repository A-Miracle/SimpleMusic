package com.ctao.music.interact.model.db;

import android.database.sqlite.SQLiteDatabase;

import com.ctao.baselib.Global;
import com.ctao.baselib.utils.LogUtils;
import com.ctao.music.model.SongInfo;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by A Miracle on 2017/7/13.
 */
public class DataBaseHelper extends OrmLiteSqliteOpenHelper{
    private static final String DB_NAME = "simple-music.db";
    private static final int DB_VERSION = 1;

    private Map<String, Dao> daos = new HashMap<>();

    private DataBaseHelper() {
        super(Global.getContext(), DB_NAME, null, DB_VERSION);
    }
    private static class Single {
        static DataBaseHelper Instance = new DataBaseHelper();
    }

    public static DataBaseHelper getInstance(){
        return Single.Instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, SongInfo.class);
        } catch (SQLException e) {
            LogUtils.e(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {

    }

    public synchronized Dao getDao(Class clazz) throws SQLException {
        Dao dao = null;
        String className = clazz.getSimpleName();
        if (daos.containsKey(className)) {
            dao = daos.get(className);
        }
        if (dao == null) {
            dao = super.getDao(clazz);
            daos.put(className, dao);
        }
        return dao;
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        daos.clear();
        super.close();
    }
}
