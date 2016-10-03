package com.raizlabs.android.dbflow.structure.database;

import android.database.sqlite.SQLiteException;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.NaturalOrderComparator;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.migration.Migration;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.ModelViewAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 */
public class BaseDatabaseHelper {

    /**
     * Location where the migration files should exist.
     */
    public static final String MIGRATION_PATH = "migrations";
    private final DatabaseDefinition databaseDefinition;

    public BaseDatabaseHelper(DatabaseDefinition databaseDefinition) {
        this.databaseDefinition = databaseDefinition;
    }

    public DatabaseDefinition getDatabaseDefinition() {
        return databaseDefinition;
    }

    public void onCreate(DatabaseWrapper db) {
        checkForeignKeySupport(db);
        executeTableCreations(db);
        executeMigrations(db, -1, db.getVersion());
        executeViewCreations(db);
    }

    public void onUpgrade(DatabaseWrapper db, int oldVersion, int newVersion) {
        checkForeignKeySupport(db);
        executeTableCreations(db);
        executeMigrations(db, oldVersion, newVersion);
        executeViewCreations(db);
    }

    public void onOpen(DatabaseWrapper db) {
        checkForeignKeySupport(db);
    }

    /**
     * If foreign keys are supported, we turn it on the DB.
     *
     * @param database
     */
    protected void checkForeignKeySupport(DatabaseWrapper database) {
        if (databaseDefinition.isForeignKeysSupported()) {
            database.execSQL("PRAGMA foreign_keys=ON;");
            FlowLog.log(FlowLog.Level.I, "Foreign Keys supported. Enabling foreign key features.");
        }
    }

    protected void executeTableCreations(final DatabaseWrapper database){
        try {
            database.beginTransaction();
            List<ModelAdapter> modelAdapters = databaseDefinition.getModelAdapters();
            for (ModelAdapter modelAdapter : modelAdapters) {
                try {
                    database.execSQL(modelAdapter.getCreationQuery());
                } catch (SQLiteException e) {
                    FlowLog.logError(e);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    protected void executeViewCreations(final DatabaseWrapper database){

        try {
            database.beginTransaction();
            List<ModelViewAdapter> modelViews = databaseDefinition.getModelViewAdapters();
            for (ModelViewAdapter modelView : modelViews) {
                QueryBuilder queryBuilder = new QueryBuilder()
                    .append("CREATE VIEW IF NOT EXISTS")
                    .appendSpaceSeparated(modelView.getViewName())
                    .append("AS ")
                    .append(modelView.getCreationQuery());
                try {
                    database.execSQL(queryBuilder.getQuery());
                } catch (SQLiteException e) {
                    FlowLog.logError(e);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    protected void executeMigrations(final DatabaseWrapper db, final int oldVersion, final int newVersion) {

        // will try migrations file or execute migrations from code
        try {
            final List<String> files = Arrays.asList(FlowManager.getContext().getAssets().list(
                    MIGRATION_PATH + "/" + databaseDefinition.getDatabaseName()));
            Collections.sort(files, new NaturalOrderComparator());

            final Map<Integer, List<String>> migrationFileMap = new HashMap<>();
            for (String file : files) {
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

            final Map<Integer, List<Migration>> migrationMap = databaseDefinition.getMigrations();

            final int curVersion = oldVersion + 1;

            try {
                db.beginTransaction();

                // execute migrations in order, migration file first before wrapped migration classes.
                for (int i = curVersion; i <= newVersion; i++) {
                    List<String> migrationFiles = migrationFileMap.get(i);
                    if (migrationFiles != null) {
                        for (String migrationFile : migrationFiles) {
                            executeSqlScript(db, migrationFile);
                            FlowLog.log(FlowLog.Level.I, migrationFile + " executed successfully.");
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
                                FlowLog.log(FlowLog.Level.I, migration.getClass() + " executed successfully.");
                            }
                        }
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (IOException e) {
            FlowLog.log(FlowLog.Level.E, "Failed to execute migrations.", e);
        }

    }

    /**
     * Supports multiline sql statements with ended with the standard ";"
     *
     * @param db   The database to run it on
     * @param file the file name in assets/migrations that we read from
     */
    private void executeSqlScript(DatabaseWrapper db, String file) {
        try {
            final InputStream input = FlowManager.getContext().getAssets().open(MIGRATION_PATH + "/" + getDatabaseDefinition().getDatabaseName() + "/" + file);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;

            // ends line with SQL
            String querySuffix = ";";

            // standard java comments
            String queryCommentPrefix = "--";
            StringBuffer query = new StringBuffer();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                boolean isEndOfQuery = line.endsWith(querySuffix);
                if (line.startsWith(queryCommentPrefix)) {
                    continue;
                }
                if (isEndOfQuery) {
                    line = line.substring(0, line.length() - querySuffix.length());
                }
                query.append(" ").append(line);
                if (isEndOfQuery) {
                    db.execSQL(query.toString());
                    query = new StringBuffer();
                }
            }

            String queryString = query.toString();
            if (queryString.trim().length() > 0) {
                db.execSQL(queryString);
            }
        } catch (IOException e) {
            FlowLog.log(FlowLog.Level.E, "Failed to execute " + file, e);
        }
    }
}
