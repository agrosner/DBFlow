package com.grosner.dbflow.cache;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.DatabaseHelperListener;
import com.grosner.dbflow.config.FlowSQLiteOpenHelper;
import com.grosner.dbflow.structure.DBStructure;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelCache {

    private DBConfiguration mDbConfiguration;

    private DBStructure mStructure;

    private FlowSQLiteOpenHelper mHelper;

    public void initialize() {
        initialize(null);
    }

    public void initialize(DatabaseHelperListener helperListener) {
        initialize(new DBConfiguration.Builder().create(), helperListener);
    }

    public void initialize(DBConfiguration dbConfiguration, DatabaseHelperListener helperListener) {
        mDbConfiguration = dbConfiguration;

        mStructure = new DBStructure(dbConfiguration);

        mHelper = new FlowSQLiteOpenHelper(dbConfiguration);
        mHelper.setDatabaseListener(helperListener);
        mHelper.getWritableDatabase();
    }

    public DBConfiguration getDbConfiguration() {
        return mDbConfiguration;
    }

    public DBStructure getStructure() {
        return mStructure;
    }

    public String getTableName(Class<? extends Model> model) {
        return mStructure.getTableStructure().get(model).getTableName();
    }

    public FlowSQLiteOpenHelper getHelper() {
        return mHelper;
    }

    public void destroy() {
        mHelper.close();
    }
}
