package com.raizlabs.android.dbflow.config;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.raizlabs.android.core.AppContext;
import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.structure.TableStructure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class FlowSQLiteOpenHelper extends SQLiteOpenHelper {

    /**
     * Location where the migration files should exist.
     */
    public final static String MIGRATION_PATH = "migrations";

    private DatabaseHelperListener mListener;

    private final boolean foreignKeysSupported;

    public FlowSQLiteOpenHelper(DBConfiguration dbConfiguration) {
        super(AppContext.getInstance(), dbConfiguration.mDatabaseName, null, dbConfiguration.mDatabaseVersion);
        movePrepackagedDB(dbConfiguration.mDatabaseName);

        foreignKeysSupported = dbConfiguration.foreignKeysSupported;
    }

    /**
     * Set a listener to listen for specific DB events and perform an action before we execute this classes
     * specific methods.
     * @param mListener
     */
    public void setDatabaseListener(DatabaseHelperListener mListener) {
        this.mListener = mListener;
    }

    /**
     * Copies over the prepackaged DB into the main DB then deletes the existing DB to save storage space.
     * @param databaseName
     */
    public void movePrepackagedDB(String databaseName) {
        final File dbPath = AppContext.getInstance().getDatabasePath(databaseName);

        // If the database already exists, return
        if (dbPath.exists()) {
            return;
        }

        // Make sure we have a path to the file
        dbPath.getParentFile().mkdirs();

        // Try to copy database file
        try {
            final InputStream inputStream = AppContext.getInstance().getAssets().open(databaseName);
            final OutputStream output = new FileOutputStream(dbPath);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            inputStream.close();

            // Delete attached DB from the app so we save space
            dbPath.delete();
        }
        catch (IOException e) {
            //AALog.e("Failed to open file", e);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if(mListener !=null) {
            mListener.onOpen(db);
        }

        checkForeignKeySupport(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if(mListener != null) {
            mListener.onCreate(db);
        }

        checkForeignKeySupport(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(mListener !=null) {
            mListener.onUpgrade(db, oldVersion, newVersion);
        }

        checkForeignKeySupport(db);
    }

    /**
     * If foreign keys are supported, we turn it on the DB.
     * @param database
     */
    private void checkForeignKeySupport(SQLiteDatabase database) {
        if(foreignKeysSupported) {
            database.execSQL("PRAGMA foreign_keys=ON;");
            //AALog.i("Foreign Keys supported. Enabling foreign key features.");
        }
    }

    /**
     * This generates the SQLite commands to create the DB
     * @param database
     */
    private void executeCreations(SQLiteDatabase database) {
        database.beginTransaction();

        Collection<TableStructure> tableStructures = FlowConfig.getCache()
                .getStructure().getTableStructure().values();
        for(TableStructure tableStructure : tableStructures) {
            database.execSQL(tableStructure.getCreationQuery().getQuery());
        }
    }
}
