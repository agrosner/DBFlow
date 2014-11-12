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
 * Description: The main interface that all Flow Managers implement. This is for internal usage only
 * as it will be generated for every {@link com.grosner.dbflow.annotation.Database}.
 */
public abstract class BaseDatabaseDefinition {

    /**
     * The helper that manages database changes and initialization
     */
    private FlowSQLiteOpenHelper mHelper;

    /**
     * Used when resetting the DB
     */
    private boolean isResetting = false;

    /**
     * Returns a list of all model classes in this database.
     * @return
     */
    public abstract List<Class<? extends Model>> getModelClasses();

    public abstract List<ModelAdapter> getModelAdapters();

    public abstract ModelAdapter getModelAdapterForTable(Class<? extends Model> table);

    public abstract ContainerAdapter getModelContainerAdapterForTable(Class<? extends Model> table);

    public abstract List<Class<? extends BaseModelView>> getModelViews();

    public abstract ModelViewAdapter getModelViewAdapterForTable(Class<? extends BaseModelView> table);

    public abstract List<ModelViewAdapter> getModelViewAdapters();

    abstract Map<Integer, List<Migration>> getMigrations();

    public SQLiteDatabase getWritableDatabase() {
        if(mHelper == null) {
            mHelper = new FlowSQLiteOpenHelper(this);
        }
        return mHelper.getWritableDatabase();
    }

    public abstract String getDatabaseName();

    public abstract int getDatabaseVersion();

    public abstract boolean areConsistencyChecksEnabled();

    public abstract boolean isForeignKeysSupported();

    public void reset(Context context) {
        if(!isResetting) {
            isResetting = true;
            context.deleteDatabase(getDatabaseName());
            isResetting = false;
        }
    }
}
