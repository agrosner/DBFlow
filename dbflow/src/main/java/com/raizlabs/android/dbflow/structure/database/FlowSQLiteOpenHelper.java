package com.raizlabs.android.dbflow.structure.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Author: andrewgrosner
 * Description: Wraps around the {@link android.database.sqlite.SQLiteOpenHelper} and provides extra features for use in this library.
 */
public class FlowSQLiteOpenHelper extends SQLiteOpenHelper implements OpenHelper {

    private DatabaseHelperDelegate backupHelper;
    private DatabaseHelperDelegate databaseHelperDelegate;
    private AndroidDatabase androidDatabase;

    public FlowSQLiteOpenHelper(BaseDatabaseDefinition databaseDefinition, DatabaseHelperListener listener) {
        super(FlowManager.getContext(), databaseDefinition.isInMemory() ? null : databaseDefinition.getDatabaseFileName(), null, databaseDefinition.getDatabaseVersion());

        if (databaseDefinition.backupEnabled()) {

            // TODO: restore backup
            //backupHelper = new DatabaseHelperDelegate()
            //// Temp database mirrors existing
            //backupHelper = new SQLiteOpenHelper(FlowManager.getContext(), getTempDbFileName(),
            //        null, databaseDefinition.getDatabaseVersion()) {
            //    @Override
            //    public void onOpen(SQLiteDatabase db) {
            //        FlowSQLiteOpenHelper.this.onOpen(db);
            //    }
//
            //    @Override
            //    public void onCreate(SQLiteDatabase db) {
            //        FlowSQLiteOpenHelper.this.onCreate(db);
            //    }
//
            //    @Override
            //    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //        FlowSQLiteOpenHelper.this.onUpgrade(db, oldVersion, newVersion);
            //    }
            //};
        }

        databaseHelperDelegate = new DatabaseHelperDelegate(listener, databaseDefinition, backupHelper);

    }

    @Override
    public DatabaseHelperDelegate getDelegate() {
        return databaseHelperDelegate;
    }

    @Override
    public boolean isDatabaseIntegrityOk() {
        return databaseHelperDelegate.isDatabaseIntegrityOk();
    }

    @Override
    public void backupDB() {
        databaseHelperDelegate.backupDB();
    }

    @Override
    public DatabaseWrapper getDatabase() {
        if (androidDatabase == null) {
            androidDatabase = AndroidDatabase.from(getWritableDatabase());
        }
        return androidDatabase;
    }

    /**
     * Set a listener to listen for specific DB events and perform an action before we execute this classes
     * specific methods.
     *
     * @param listener
     */
    public void setDatabaseListener(DatabaseHelperListener listener) {
        databaseHelperDelegate.setDatabaseHelperListener(listener);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        databaseHelperDelegate.onCreate(AndroidDatabase.from(db));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        databaseHelperDelegate.onUpgrade(AndroidDatabase.from(db), oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        databaseHelperDelegate.onOpen(AndroidDatabase.from(db));
    }

}
