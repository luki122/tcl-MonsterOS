package cn.tcl.weather.provider.cityname;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.leon.tools.view.AndroidUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-12.
 * $desc
 */
public class CityDataHelper extends OrmLiteSqliteOpenHelper {

    private final static Object[] HOT_CITY_IDS = new Integer[]{
            1, 16, 2191, 2224, 2228, 1332, 1348,
            1502, 752, 758, 1208, 2026, 1410, 38,
            1674, 651, 2431, 72, 212, 207, 156};

    private final static String TAG = "CityDataHelper";
    private final static int MAX_SIZE = 20;
    public final static String DB_NAME = "cities_db.db";
    private Context mContext;

    public CityDataHelper(Context context) {
        super(context, DB_NAME, null, 1);
        mContext = context;
    }

    private void resetDb() {
        File file = mContext.getDatabasePath(DB_NAME);
        if (!file.exists()) {
            try {
                AndroidUtils.copyFile(mContext.getAssets().open(DB_NAME), file.getAbsolutePath());
            } catch (IOException e) {
                LogUtils.d(TAG, "resetDb err: " + e.toString());
            }
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        synchronized (this) {
            resetDb();
            return super.getReadableDatabase();
        }
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        synchronized (this) {
            resetDb();
            return super.getWritableDatabase();
        }
    }


    private static void queueForList(Dao<CityNameBean, Integer> dao, String[] keys, String value, int maxSize, List<CityNameBean> beans) throws SQLException {
        final int size = maxSize - beans.size();
        if (size > 0) {
            QueryBuilder<CityNameBean, Integer> queryBuilder = dao.queryBuilder();
            Where<CityNameBean, Integer> where = queryBuilder.where();
            where.like(keys[0], value).or().like(keys[1], value);

            for (CityNameBean bean : queryBuilder.limit(size).query()) {
                if (!beans.contains(bean)) {
                    beans.add(bean);
                }
            }
        }
    }

    public List<CityNameBean> queueForList(String name) throws SQLException {
        List<CityNameBean> beans = new ArrayList<>(MAX_SIZE);
        Dao<CityNameBean, Integer> dao = getDao(CityNameBean.class);

        String name1 = name + "%";
        String name2 = "%" + name + "%";

        String[] keys = new String[]{CityNameBean.COUNTY_EN, CityNameBean.COUNTY_CN};
        queueForList(dao, keys, name1, MAX_SIZE, beans);
        queueForList(dao, keys, name2, MAX_SIZE, beans);

        keys = new String[]{CityNameBean.CITY_EN, CityNameBean.CITY_CN};
        queueForList(dao, keys, name1, MAX_SIZE, beans);
        queueForList(dao, keys, name2, MAX_SIZE, beans);

        keys = new String[]{CityNameBean.PROVINCE_EN, CityNameBean.PROVINCE_CN};
        queueForList(dao, keys, name1, MAX_SIZE, beans);
        queueForList(dao, keys, name2, MAX_SIZE, beans);

        keys = new String[]{CityNameBean.COUNTRY_EN, CityNameBean.COUNTRY_CN};
        queueForList(dao, keys, name1, MAX_SIZE, beans);
        queueForList(dao, keys, name2, MAX_SIZE, beans);

        return beans;
    }

    public List<CityNameBean> queueForHotCities() throws SQLException {
        Dao<CityNameBean, Integer> dao = getDao(CityNameBean.class);
        QueryBuilder<CityNameBean, Integer> queryBuilder = dao.queryBuilder();
        queryBuilder.where().in(CityNameBean.ID, HOT_CITY_IDS);
        return queryBuilder.query();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {

    }
}
