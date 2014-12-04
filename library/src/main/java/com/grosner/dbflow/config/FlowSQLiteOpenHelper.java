package com.grosner.dbflow.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.grosner.dbflow.DatabaseHelperListener;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.transaction.BaseTransaction;
import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.dbflow.sql.migration.Migration;
import com.grosner.dbflow.structure.ModelAdapter;
import com.grosner.dbflow.structure.ModelViewAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Description:
 */
public class FlowSQLiteOpenHelper extends SQLiteOpenHelper {

    public final static String TEMP_DB_NAME = "temp-";

    /**
     * Location where the migration files should exist.
     */
    public final static String MIGRATION_PATH = "migrations";
    private DatabaseHelperListener mListener;
    private BaseDatabaseDefinition mManager;

    private SQLiteOpenHelper mBackupDB;

    public FlowSQLiteOpenHelper(BaseDatabaseDefinition flowManager, DatabaseHelperListener listener) {
        super(FlowManager.getContext(), flowManager.getDatabaseName() + ".db", null, flowManager.getDatabaseVersion());
        mListener = listener;
        mManager = flowManager;

        movePrepackagedDB(getDatabaseFileName(), getDatabaseFileName());

        if (flowManager.backupEnabled()) {
            // Temp database mirrors existing
            mBackupDB = new SQLiteOpenHelper(FlowManager.getContext(), getTempDbFileName(),
                    null, flowManager.getDatabaseVersion()) {
                @Override
                public void onOpen(SQLiteDatabase db) {
                    checkForeignKeySupport(db);
                }

                @Override
                public void onCreate(SQLiteDatabase db) {
                    checkForeignKeySupport(db);
                    executeCreations(db);
                    executeMigrations(db, -1, db.getVersion());
                }

                @Override
                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                    checkForeignKeySupport(db);
                    executeCreations(db);
                    executeMigrations(db, oldVersion, newVersion);
                }
            };
            restoreDatabase(getTempDbFileName(), getDatabaseFileName());
            mBackupDB.getWritableDatabase();
        }
    }

    /**
     * @return the temporary database file name for when we have backups enabled {@link BaseDatabaseDefinition#backupEnabled()}
     */
    private String getTempDbFileName() {
        return TEMP_DB_NAME + mManager.getDatabaseName() + ".db";
    }

    private String getDatabaseFileName() {
        return mManager.getDatabaseName() + ".db";
    }


    /**
     * Pulled partially from code, runs the integrity check on pre-honeycomb devices.
     *
     * @return
     */
    public boolean isDatabaseIntegrityOk() {
        boolean integrityOk = true;

        SQLiteStatement prog = null;
        try {
            prog = getWritableDatabase().compileStatement("PRAGMA quick_check(1)");
            String rslt = prog.simpleQueryForString();
            if (!rslt.equalsIgnoreCase("ok")) {
                // integrity_checker failed on main or attached databases
                FlowLog.log(FlowLog.Level.E, "PRAGMA integrity_check on " + mManager.getDatabaseName() + " returned: " + rslt);

                integrityOk = false;

                if (mManager.backupEnabled()) {
                    integrityOk = restoreBackUp();
                }
            }
        } finally {
            if (prog != null) {
                prog.close();
            }
        }
        return integrityOk;
    }

    /**
     * Will use the already existing app database if {@link BaseDatabaseDefinition#backupEnabled()} is true. If the existing
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
            File existingDb = FlowManager.getContext().getDatabasePath(getDatabaseFileName());
            InputStream inputStream;
            // if it exists and the integrity is ok
            if (existingDb.exists() && (mManager.backupEnabled() && FlowManager.isDatabaseIntegrityOk(mBackupDB))) {
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
     * If integrity check fails, this method will use the backup db to fix itself. In order to prevent
     * loss of data, please backup often!
     */
    public boolean restoreBackUp() {
        boolean success = true;

        File db = FlowManager.getContext().getDatabasePath(TEMP_DB_NAME + mManager.getDatabaseName());
        File corrupt = FlowManager.getContext().getDatabasePath(mManager.getDatabaseName());
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
     * Writes the inputstream of the existing db to the file specified.
     * @param dbPath
     * @param existingDB
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
     * Copies over the prepackaged DB into the main DB then deletes the existing DB to save storage space. If
     * we have a backup that exists
     *
     * @param databaseName
     */
    public void movePrepackagedDB(String databaseName, String prepackagedName) {
        final File dbPath = FlowManager.getContext().getDatabasePath(databaseName);

        // If the database already exists, and is ok return
        if (dbPath.exists() && (!mManager.areConsistencyChecksEnabled() || (mManager.areConsistencyChecksEnabled() && isDatabaseIntegrityOk()))) {
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
            if (existingDb.exists() && (!mManager.backupEnabled() || mManager.backupEnabled() && FlowManager.isDatabaseIntegrityOk(mBackupDB))) {
                inputStream = new FileInputStream(existingDb);
            } else {
                inputStream = FlowManager.getContext().getAssets().open(prepackagedName);
            }
            writeDB(dbPath, inputStream);

        } catch (IOException e) {
            FlowLog.log(FlowLog.Level.E, "Failed to open file", e);
        }
    }

    /**
     * Saves the database as a backup on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}. This will
     * create a THIRD database to use as a backup to the backup in case somehow the overwrite fails.
     */
    public void backupDB() {
        if(!mManager.backupEnabled() || !mManager.areConsistencyChecksEnabled()) {
            throw new IllegalStateException("Backups are not enabled for : " + mManager.getDatabaseName() + ". Please consider adding " +
                    "both backupEnabled and consistency checks enabled to the Database annotation");
        }
        // highest priority ever!
        TransactionManager.getInstance().addTransaction(new BaseTransaction(DBTransactionInfo.create(BaseTransaction.PRIORITY_UI + 1)) {
            @Override
            public Object onExecute() {

                Context context = FlowManager.getContext();
                File backup = context.getDatabasePath(getTempDbFileName());
                File temp = context.getDatabasePath(TEMP_DB_NAME + "-2-" + getDatabaseFileName());

                // if exists we want to delete it before rename
                if (temp.exists()) {
                    temp.delete();
                }

                backup.renameTo(temp);
                if (backup.exists()) {
                    backup.delete();
                }
                File existing = context.getDatabasePath(getDatabaseFileName());

                try {
                    backup.getParentFile().mkdirs();
                    writeDB(backup, new FileInputStream(existing));

                    temp.delete();
                } catch (Exception e) {
                    FlowLog.logError(e);

                }
                return null;
            }

        });
    }

    /**
     * Set a listener to listen for specific DB events and perform an action before we execute this classes
     * specific methods.
     *
     * @param mListener
     */
    public void setDatabaseListener(DatabaseHelperListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (mListener != null) {
            mListener.onCreate(db);
        }

        checkForeignKeySupport(db);
        executeCreations(db);
        executeMigrations(db, -1, db.getVersion());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (mListener != null) {
            mListener.onUpgrade(db, oldVersion, newVersion);
        }

        checkForeignKeySupport(db);
        executeCreations(db);
        executeMigrations(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        if (mListener != null) {
            mListener.onOpen(db);
        }

        checkForeignKeySupport(db);
    }

    /**
     * If foreign keys are supported, we turn it on the DB.
     *
     * @param database
     */
    private void checkForeignKeySupport(SQLiteDatabase database) {
        if (mManager.isForeignKeysSupported()) {
            database.execSQL("PRAGMA foreign_keys=ON;");
            FlowLog.log(FlowLog.Level.I, "Foreign Keys supported. Enabling foreign key features.");
        }
    }

    /**
     * This generates the SQLite commands to create the DB
     *
     * @param database
     */
    private void executeCreations(final SQLiteDatabase database) {

        TransactionManager.transact(database, new Runnable() {
            @Override
            public void run() {

                List<ModelAdapter> modelAdapters = mManager.getModelAdapters();
                for (ModelAdapter modelAdapter : modelAdapters) {
                    database.execSQL(modelAdapter.getCreationQuery());
                }

                // create our model views
                List<ModelViewAdapter> modelViews = mManager.getModelViewAdapters();
                for (ModelViewAdapter modelView : modelViews) {
                    QueryBuilder queryBuilder = new QueryBuilder()
                            .append("CREATE VIEW")
                            .appendSpaceSeparated(modelView.getViewName())
                            .append("AS ")
                            .append(modelView.getCreationQuery());
                    try {
                        database.execSQL(queryBuilder.getQuery());
                    } catch (SQLiteException e) {
                        FlowLog.logError(e);
                    }
                }
            }
        });
    }

    private void executeMigrations(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

        // will try migrations file or execute migrations from code
        try {
            final List<String> files = Arrays.asList(FlowManager.getContext().getAssets().list(MIGRATION_PATH));
            Collections.sort(files, new NaturalOrderComparator());

            final Map<Integer, List<String>> migrationFileMap = new HashMap<>();
            for(String file: files) {
                try {
                    final Integer version = Integer.valueOf(file.replace(".sql", ""));
                    List<String> fileList = migrationFileMap.get(version);
                    if (fileList == null) {
                        fileList = new ArrayList<>();
                        migrationFileMap.put(version, fileList);
                    }
                    fileList.add(file);
                } catch (NumberFormatException e) {
                    FlowLog.log(FlowLog.Level.W, "Skipping invalidly named file: " + file, e);
                }
            }

            final Map<Integer, List<Migration>> migrationMap = mManager.getMigrations();

            final int curVersion = oldVersion + 1;

            TransactionManager.transact(db, new Runnable() {
                @Override
                public void run() {

                    // execute migrations in order, migration file first before wrapped migration classes.
                    for (int i = curVersion; i <= newVersion; i++) {
                        List<String> migrationFiles = migrationFileMap.get(i);
                        if (migrationFiles != null) {
                            for (String migrationFile : migrationFiles) {
                                executeSqlScript(db, migrationFile);
                                FlowLog.log(FlowLog.Level.I, migrationFile + " executed succesfully.");
                            }
                        }

                        if (migrationMap != null) {
                            List<Migration> migrationsList = migrationMap.get(i);
                            if (migrationsList != null) {
                                for (Migration migration : migrationsList) {

                                    // before migration
                                    migration.onPreMigrate();

                                    // migrate
                                    migration.migrate(db);

                                    // after migration cleanup
                                    migration.onPostMigrate();
                                }
                            }
                        }

                    }
                }
            });
        } catch (IOException e) {
            FlowLog.log(FlowLog.Level.E, "Failed to execute migrations.", e);
        }
    }

    /**
     * Supports multiline sql statements with ended with the standard ";"
     * @param db The database to run it on
     * @param file the file name in assets/migrations that we read from
     */
    private void executeSqlScript(SQLiteDatabase db, String file) {
        try {
            final InputStream input = FlowManager.getContext().getAssets().open(MIGRATION_PATH + "/" + file);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;

            // ends line with SQL
            String querySuffix= ";";

            // standard java comments
            String queryCommentPrefix = "\\";
            StringBuffer query = new StringBuffer();

            while((line = reader.readLine()) != null){
                line = line.trim();
                boolean isEndOfQuery = line.endsWith(querySuffix);
                if(line.startsWith(queryCommentPrefix)){
                    continue;
                }
                if(isEndOfQuery){
                    line = line.substring(0, line.length()-querySuffix.length());
                }
                query.append(" ").append(line);
                if(isEndOfQuery){
                    db.execSQL(query.toString());
                    query = new StringBuffer();
                }
            }

            if(query.length() > 0){
                db.execSQL(query.toString());
            }
        } catch (IOException e) {
            FlowLog.log(FlowLog.Level.E, "Failed to execute " + file, e);
        }
    }
}
