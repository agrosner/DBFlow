package com.raizlabs.android.dbflow.sqlcipher;

import android.content.Context;

import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.BaseDatabaseHelper;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperDelegate;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * Description: The replacement {@link OpenHelper} for SQLCipher. Specify a subclass of this is {@link DatabaseConfig#databaseClass()}
 * of your database to get it to work with specifying the secret you use for the database.
 */
public abstract class SQLCipherOpenHelper extends SQLiteOpenHelper implements OpenHelper {

    private DatabaseHelperDelegate databaseHelperDelegate;
    private SQLCipherDatabase cipherDatabase;

    public SQLCipherOpenHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener listener) {
        super(FlowManager.getContext(), databaseDefinition.isInMemory() ? null : databaseDefinition.getDatabaseFileName(), null, databaseDefinition.getDatabaseVersion());
        SQLiteDatabase.loadLibs(FlowManager.getContext());

        OpenHelper backupHelper = null;
        if (databaseDefinition.backupEnabled()) {
            // Temp database mirrors existing
            backupHelper = new BackupHelper(FlowManager.getContext(), DatabaseHelperDelegate.getTempDbFileName(databaseDefinition),
                databaseDefinition.getDatabaseVersion(), databaseDefinition);
        }

        databaseHelperDelegate = new DatabaseHelperDelegate(listener, databaseDefinition, backupHelper);
    }

    @Override
    public void performRestoreFromBackup() {
        databaseHelperDelegate.performRestoreFromBackup();
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
        if (cipherDatabase == null || !cipherDatabase.getDatabase().isOpen()) {
            cipherDatabase = SQLCipherDatabase.from(getWritableDatabase(getCipherSecret()));
        }
        return cipherDatabase;
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
        databaseHelperDelegate.onCreate(SQLCipherDatabase.from(db));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        databaseHelperDelegate.onUpgrade(SQLCipherDatabase.from(db), oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        databaseHelperDelegate.onOpen(SQLCipherDatabase.from(db));
    }

    @Override
    public void closeDB() {
        getDatabase();
        cipherDatabase.getDatabase().close();
    }

    /**
     * @return The SQLCipher secret for opening this database.
     */
    protected abstract String getCipherSecret();

    /**
     * Simple helper to manage backup.
     */
    private class BackupHelper extends SQLiteOpenHelper implements OpenHelper {

        private SQLCipherDatabase sqlCipherDatabase;
        private final BaseDatabaseHelper baseDatabaseHelper;

        public BackupHelper(Context context, String name, int version, DatabaseDefinition databaseDefinition) {
            super(context, name, null, version);
            this.baseDatabaseHelper = new BaseDatabaseHelper(databaseDefinition);
        }

        @Override
        public DatabaseWrapper getDatabase() {
            if (sqlCipherDatabase == null) {
                sqlCipherDatabase = SQLCipherDatabase.from(getWritableDatabase(getCipherSecret()));
            }
            return sqlCipherDatabase;
        }

        @Override
        public void performRestoreFromBackup() {
        }

        @Override
        public DatabaseHelperDelegate getDelegate() {
            return null;
        }

        @Override
        public boolean isDatabaseIntegrityOk() {
            return false;
        }

        @Override
        public void backupDB() {
        }

        @Override
        public void closeDB() {
        }

        @Override
        public void setDatabaseListener(DatabaseHelperListener helperListener) {
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            baseDatabaseHelper.onCreate(SQLCipherDatabase.from(db));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            baseDatabaseHelper.onUpgrade(SQLCipherDatabase.from(db), oldVersion, newVersion);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            baseDatabaseHelper.onOpen(SQLCipherDatabase.from(db));
        }
    }
}
