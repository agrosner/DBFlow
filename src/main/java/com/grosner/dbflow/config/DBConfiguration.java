package com.grosner.dbflow.config;

import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DBConfiguration {

    static final int DEFAULT_CACHE_SIZE = 1024;

    String mDatabaseName;

    int mDatabaseVersion;

    int mCacheSize;

    List<Class<? extends Model>> mModelClasses;

    boolean foreignKeysSupported = false;

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public int getDatabaseVersion() {
        return mDatabaseVersion;
    }

    public int getCacheSize() {
        return mCacheSize;
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

    public static class Builder {

        private DBConfiguration mConfiguration;

        public Builder() {
            mConfiguration = new DBConfiguration();
            mConfiguration.mCacheSize = DEFAULT_CACHE_SIZE;
        }

        public Builder cacheSize(int size) {
            mConfiguration.mCacheSize = size;
            return this;
        }

        public Builder databaseName(String databaseName) {
            mConfiguration.mDatabaseName = databaseName;
            return this;
        }

        public Builder databaseVersion(int databaseVersion) {
            mConfiguration.mDatabaseVersion = databaseVersion;
            return this;
        }

        public Builder addModelClass(Class<? extends Model> modelClass) {
            if(mConfiguration.mModelClasses == null) {
                mConfiguration.mModelClasses = new ArrayList<Class<? extends Model>>();
            }

            mConfiguration.mModelClasses.add(modelClass);

            return this;
        }

        public Builder foreignKeysSupported() {
            mConfiguration.foreignKeysSupported = true;
            return this;
        }

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

        public DBConfiguration create() {
            return mConfiguration;
        }
    }
}
