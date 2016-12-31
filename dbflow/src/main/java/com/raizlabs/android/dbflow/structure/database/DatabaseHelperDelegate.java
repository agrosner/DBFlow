package com.raizlabs.android.dbflow.structure.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Description: An abstraction from some parts of the {@link SQLiteOpenHelper} where this can be
 * used in other helper class definitions.
 */
public class DatabaseHelperDelegate extends BaseDatabaseHelper {

    public static final String TEMP_DB_NAME = "temp-";

    public static String getTempDbFileName(DatabaseDefinition databaseDefinition) {
        return TEMP_DB_NAME + databaseDefinition.getDatabaseName() + ".db";
    }

    private DatabaseHelperListener databaseHelperListener;

    @Nullable private final OpenHelper backupHelper;

    public DatabaseHelperDelegate(DatabaseHelperListener databaseHelperListener,
                                  DatabaseDefinition databaseDefinition, @Nullable OpenHelper backupHelper) {
        super(databaseDefinition);
        this.databaseHelperListener = databaseHelperListener;
        this.backupHelper = backupHelper;
    }

    public void performRestoreFromBackup() {
        movePrepackagedDB(getDatabaseDefinition().getDatabaseFileName(),
                getDatabaseDefinition().getDatabaseFileName());

        if (getDatabaseDefinition().backupEnabled()) {
            if (backupHelper == null) {
                throw new IllegalStateException("the passed backup helper was null, even though backup is enabled. " +
                        "Ensure that its passed in.");
            }
            restoreDatabase(getTempDbFileName(), getDatabaseDefinition().getDatabaseFileName());
            backupHelper.getDatabase();
        }
    }

    /**
     * @param databaseHelperListener Listens for operations the DB and allow you to provide extra
     *                               functionality.
     */
    public void setDatabaseHelperListener(DatabaseHelperListener databaseHelperListener) {
        this.databaseHelperListener = databaseHelperListener;
    }

    @Override
    public void onCreate(DatabaseWrapper db) {
        if (databaseHelperListener != null) {
            databaseHelperListener.onCreate(db);
        }
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(DatabaseWrapper db, int oldVersion, int newVersion) {
        if (databaseHelperListener != null) {
            databaseHelperListener.onUpgrade(db, oldVersion, newVersion);
        }
        super.onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(DatabaseWrapper db) {
        if (databaseHelperListener != null) {
            databaseHelperListener.onOpen(db);
        }
        super.onOpen(db);
    }

    /**
     * @return the temporary database file name for when we have backups enabled
     * {@link DatabaseDefinition#backupEnabled()}
     */
    private String getTempDbFileName() {
        return getTempDbFileName(getDatabaseDefinition());
    }

    /**
     * Copies over the prepackaged DB into the main DB then deletes the existing DB to save storage space. If
     * we have a backup that exists
     *
     * @param databaseName    The name of the database to copy over
     * @param prepackagedName The name of the prepackaged db file
     */
    public void movePrepackagedDB(String databaseName, String prepackagedName) {
        final File dbPath = FlowManager.getContext().getDatabasePath(databaseName);

        // If the database already exists, and is ok return
        if (dbPath.exists() && (!getDatabaseDefinition().areConsistencyChecksEnabled() ||
                (getDatabaseDefinition().areConsistencyChecksEnabled()
                        && isDatabaseIntegrityOk(getWritableDatabase())))) {
            return;
        }

        // Make sure we have a path to the file
        dbPath.getParentFile().mkdirs();

        // Try to copy database file
        try {
            // check existing and use that as backup
            File existingDb = FlowManager.getContext().getDatabasePath(getTempDbFileName());
            InputStream inputStream;
            // if it exists and the integrity is ok we use backup as the main DB is no longer valid
            if (existingDb.exists() && (!getDatabaseDefinition().backupEnabled() || getDatabaseDefinition().backupEnabled()
                    && backupHelper != null && isDatabaseIntegrityOk(backupHelper.getDatabase()))) {
                inputStream = new FileInputStream(existingDb);
            } else {
                inputStream = FlowManager.getContext().getAssets().open(prepackagedName);
            }
            writeDB(dbPath, inputStream);

        } catch (IOException e) {
            FlowLog.log(FlowLog.Level.W, "Failed to open file", e);
        }
    }

    /**
     * Pulled partially from code, it runs a "PRAGMA quick_check(1)" to see if the database is ok.
     * This method will {@link #restoreBackUp()} if they are enabled on the database if this check fails. So
     * use with caution and ensure that you backup the database often!
     *
     * @return true if the database is ok, false if the consistency has been compromised.
     */
    public boolean isDatabaseIntegrityOk() {
        return isDatabaseIntegrityOk(getWritableDatabase());
    }

    /**
     * Pulled partially from code, it runs a "PRAGMA quick_check(1)" to see if the database is ok.
     * This method will {@link #restoreBackUp()} if they are enabled on the database if this check fails. So
     * use with caution and ensure that you backup the database often!
     *
     * @return true if the database is ok, false if the consistency has been compromised.
     */
    public boolean isDatabaseIntegrityOk(DatabaseWrapper databaseWrapper) {
        boolean integrityOk = true;

        DatabaseStatement statement = null;
        try {
            statement = databaseWrapper.compileStatement("PRAGMA quick_check(1)");
            String result = statement.simpleQueryForString();
            if (!result.equalsIgnoreCase("ok")) {
                // integrity_checker failed on main or attached databases
                FlowLog.log(FlowLog.Level.E, "PRAGMA integrity_check on " +
                        getDatabaseDefinition().getDatabaseName() + " returned: " + result);

                integrityOk = false;

                if (getDatabaseDefinition().backupEnabled()) {
                    integrityOk = restoreBackUp();
                }
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return integrityOk;
    }

    /**
     * If integrity check fails, this method will use the backup db to fix itself. In order to prevent
     * loss of data, please backup often!
     */
    public boolean restoreBackUp() {
        boolean success = true;

        File db = FlowManager.getContext().getDatabasePath(TEMP_DB_NAME + getDatabaseDefinition().getDatabaseName());
        File corrupt = FlowManager.getContext().getDatabasePath(getDatabaseDefinition().getDatabaseName());
        if (corrupt.delete()) {
            try {
                writeDB(corrupt, new FileInputStream(db));
            } catch (IOException e) {
                FlowLog.logError(e);
                success = false;
            }
        } else {
            FlowLog.log(FlowLog.Level.E, "Failed to delete DB");
        }
        return success;
    }

    /**
     * Writes the {@link InputStream} of the existing db to the file specified.
     *
     * @param dbPath     The file to write to.
     * @param existingDB The existing database file's input stream¬
     * @throws IOException
     */
    private void writeDB(File dbPath, InputStream existingDB) throws IOException {
        final OutputStream output = new FileOutputStream(dbPath);

        byte[] buffer = new byte[1024];
        int length;

        while ((length = existingDB.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        existingDB.close();
    }

    /**
     * Will use the already existing app database if {@link DatabaseDefinition#backupEnabled()} is true. If the existing
     * is not there we will try to use the prepackaged database for that purpose.
     *
     * @param databaseName    The name of the database to restore
     * @param prepackagedName The name of the prepackaged db file
     */
    public void restoreDatabase(String databaseName, String prepackagedName) {
        final File dbPath = FlowManager.getContext().getDatabasePath(databaseName);

        // If the database already exists, return
        if (dbPath.exists()) {
            return;
        }

        // Make sure we have a path to the file
        dbPath.getParentFile().mkdirs();

        // Try to copy database file
        try {
            // check existing and use that as backup
            File existingDb = FlowManager.getContext().getDatabasePath(getDatabaseDefinition().getDatabaseFileName());
            InputStream inputStream;
            // if it exists and the integrity is ok
            if (existingDb.exists() && (getDatabaseDefinition().backupEnabled()
                    && backupHelper != null && isDatabaseIntegrityOk(backupHelper.getDatabase()))) {
                inputStream = new FileInputStream(existingDb);
            } else {
                inputStream = FlowManager.getContext().getAssets().open(prepackagedName);
            }
            writeDB(dbPath, inputStream);
        } catch (IOException e) {
            FlowLog.logError(e);
        }
    }


    /**
     * Saves the database as a backup on the {@link DefaultTransactionQueue}.
     * This will create a THIRD database to use as a backup to the backup in case somehow the overwrite fails.
     */
    public void backupDB() {
        if (!getDatabaseDefinition().backupEnabled() || !getDatabaseDefinition().areConsistencyChecksEnabled()) {
            throw new IllegalStateException("Backups are not enabled for : " + getDatabaseDefinition().getDatabaseName() + ". Please consider adding " +
                    "both backupEnabled and consistency checks enabled to the Database annotation");
        }

        getDatabaseDefinition().beginTransactionAsync(new ITransaction() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
                Context context = FlowManager.getContext();
                File backup = context.getDatabasePath(getTempDbFileName());
                File temp = context.getDatabasePath(TEMP_DB_NAME + "-2-" + getDatabaseDefinition().getDatabaseFileName());

                // if exists we want to delete it before rename
                if (temp.exists()) {
                    temp.delete();
                }

                backup.renameTo(temp);
                if (backup.exists()) {
                    backup.delete();
                }
                File existing = context.getDatabasePath(getDatabaseDefinition().getDatabaseFileName());

                try {
                    backup.getParentFile().mkdirs();
                    writeDB(backup, new FileInputStream(existing));

                    temp.delete();
                } catch (Exception e) {
                    FlowLog.logError(e);

                }
            }
        }).build().execute();

    }

    public DatabaseWrapper getWritableDatabase() {
        return getDatabaseDefinition().getWritableDatabase();
    }
}
