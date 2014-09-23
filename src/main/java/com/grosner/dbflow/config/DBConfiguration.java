package com.grosner.dbflow.config;

import android.util.SparseArray;

import com.grosner.dbflow.sql.migration.Migration;
import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Defines the configuration for the database including version, name, if foreign keys are supported,
 * and can optionally specify the Model Classes for the database manually (instead of searching in class files).
 */
public class DBConfiguration {

    /**
     * The name of this database in private app data
     */
    String mDatabaseName;

    /**
     * The version of the database
     */
    int mDatabaseVersion;

    /**
     * The optional list of model classes that this DB will use
     */
    List<Class<? extends Model>> mModelClasses;

    /**
     * The list of DB migrations we can add to the DB when necessary
     */
    SparseArray<List<Migration>> mMigrations;

    /**
     * Whether we want to support foreign keys
     */
    boolean foreignKeysSupported = false;

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public int getDatabaseVersion() {
        return mDatabaseVersion;
    }

    public List<Class<? extends Model>> getModelClasses() {
        return mModelClasses;
    }

    public boolean hasModelClasses() {
        return mModelClasses != null && !mModelClasses.isEmpty();
    }

    public boolean isForeignKeysSupported() {
        return foreignKeysSupported;
    }

    /**
     * Used to build the database configuration.
     */
    public static class Builder {

        /**
         * The inner configuration we will use.
         */
        private DBConfiguration mConfiguration;

        /**
         * Constructs a new instance of this class and {@link com.grosner.dbflow.config.DBConfiguration}
         */
        public Builder() {
            mConfiguration = new DBConfiguration();
        }

        /**
         * Specify the database name. The .db is not necessary. This also must match any prepackaged database.
         *
         * @param databaseName The name of the database in private app data
         * @return The builder
         */
        public Builder databaseName(String databaseName) {
            mConfiguration.mDatabaseName = databaseName +".db";
            return this;
        }

        /**
         * The db version. Incrementing this value will trigger
         * {@link com.grosner.dbflow.DatabaseHelperListener#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)}
         * .
         *
         * @param databaseVersion
         * @return
         */
        public Builder databaseVersion(int databaseVersion) {
            mConfiguration.mDatabaseVersion = databaseVersion;
            return this;
        }

        /**
         * Adds specific model classes to be evaluated (or created with a new db)
         *
         * @param modelClasses
         * @return
         */
        public Builder addModelClasses(Class<? extends Model>... modelClasses) {
            if (mConfiguration.mModelClasses == null) {
                mConfiguration.mModelClasses = new ArrayList<Class<? extends Model>>();
            }

            mConfiguration.mModelClasses.addAll(Arrays.asList(modelClasses));
            return this;
        }

        public Builder setModelClasses(Class<? extends Model>... modelClasses) {
            mConfiguration.mModelClasses = Arrays.asList(modelClasses);
            return this;
        }

        /**
         * Add migrations to this builder.
         *
         * @param migrations The migrations to be executed when we create and upgrade the DB
         * @return
         */
        public Builder addMigrations(Migration... migrations) {
            if (mConfiguration.mMigrations == null) {
                mConfiguration.mMigrations = new SparseArray<List<Migration>>();
            }

            for (Migration migration : migrations) {

                List<Migration> migrationList = mConfiguration.mMigrations.get(migration.getNewVersion());
                if (migrationList == null) {
                    migrationList = new ArrayList<Migration>();
                }

                migrationList.add(migration);
            }

            return this;
        }

        /**
         * Will make foreign keys supported in this DB.
         *
         * @return
         */
        public Builder foreignKeysSupported() {
            mConfiguration.foreignKeysSupported = true;
            return this;
        }

        public DBConfiguration create() {
            return mConfiguration;
        }
    }
}
