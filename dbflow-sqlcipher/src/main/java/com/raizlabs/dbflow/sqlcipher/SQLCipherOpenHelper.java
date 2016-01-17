package com.raizlabs.dbflow.sqlcipher;

import android.content.Context;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperDelegate;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * Description: The replacement {@link OpenHelper} for SQLCipher. Specify this is {@link Database#sqlHelperClass()}
 * of your database to get it to work.
 */
public abstract class SQLCipherOpenHelper extends SQLiteOpenHelper implements OpenHelper {

    private DatabaseHelperDelegate databaseHelperDelegate;
    private SQLCipherDatabase cipherDatabase;

    public SQLCipherOpenHelper(BaseDatabaseDefinition databaseDefinition, DatabaseHelperListener listener) {
        super(FlowManager.getContext(), databaseDefinition.isInMemory() ? null : databaseDefinition.getDatabaseFileName(), null, databaseDefinition.getDatabaseVersion());

        OpenHelper backupHelper = null;
        if (databaseDefinition.backupEnabled()) {
            // Temp database mirrors existing
            backupHelper = new BackupHelper(FlowManager.getContext(), DatabaseHelperDelegate.getTempDbFileName(databaseDefinition),
                    databaseDefinition.getDatabaseVersion()) {
                @Override
                public void onOpen(SQLiteDatabase db) {
                    SQLCipherOpenHelper.this.onOpen(db);
                }

                @Override
                public void onCreate(SQLiteDatabase db) {
                    SQLCipherOpenHelper.this.onCreate(db);
                }

                @Override
                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                    SQLCipherOpenHelper.this.onUpgrade(db, oldVersion, newVersion);
                }
            };
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
        if (cipherDatabase == null) {
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

    /**
     * @return The SQLCipher secret for opening this database.
     */
    protected abstract String getCipherSecret();

    /**
     * Simple helper to manage backup.
     */
    private abstract class BackupHelper extends SQLiteOpenHelper implements OpenHelper {

        private SQLCipherDatabase sqlCipherDatabase;

        public BackupHelper(Context context, String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public DatabaseWrapper getDatabase() {
            if (sqlCipherDatabase == null) {
                sqlCipherDatabase = SQLCipherDatabase.from(getWritableDatabase(getCipherSecret()));
            }
            return sqlCipherDatabase;
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
        public void setDatabaseListener(DatabaseHelperListener helperListener) {
        }
    }
}
