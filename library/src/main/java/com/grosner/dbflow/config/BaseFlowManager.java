package com.grosner.dbflow.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.grosner.dbflow.sql.migration.Migration;
import com.grosner.dbflow.structure.BaseModelView;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelAdapter;
import com.grosner.dbflow.structure.ModelViewAdapter;
import com.grosner.dbflow.structure.container.ContainerAdapter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: The main interface that all Flow Managers implement.
 */
public abstract class BaseFlowManager {

    private FlowSQLiteOpenHelper mHelper;

    private boolean isResetting = false;

    public abstract List<Class<? extends Model>> getModelClasses();

    public abstract List<ModelAdapter> getModelAdapters();

    public abstract ModelAdapter getModelAdapterForTable(Class<? extends Model> table);

    public abstract ContainerAdapter getModelContainerAdapterForTable(Class<? extends Model> table);

    public abstract List<Class<? extends BaseModelView>> getModelViews();

    public abstract ModelViewAdapter getModelViewAdapterForTable(Class<? extends BaseModelView> table);

    public abstract List<ModelViewAdapter> getModelViewAdapters();

    abstract Map<Integer, List<Migration>> getMigrations();

    public abstract boolean isForeignKeysSupported();

    public SQLiteDatabase getWritableDatabase() {
        if(mHelper == null) {
            mHelper = new FlowSQLiteOpenHelper(this);
        }
        return mHelper.getWritableDatabase();
    }

    public abstract String getDatabaseName();

    public abstract int getDatabaseVersion();

    public void reset(Context context) {
        if(!isResetting) {
            isResetting = true;
            context.deleteDatabase(getDatabaseName());
            isResetting = false;
        }
    }
}
