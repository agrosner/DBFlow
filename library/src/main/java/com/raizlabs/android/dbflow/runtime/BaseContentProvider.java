package com.raizlabs.android.dbflow.runtime;

import android.content.ContentProvider;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Description:
 */
public abstract class BaseContentProvider extends ContentProvider {

    protected BaseDatabaseDefinition database;

    @Override
    public boolean onCreate() {
        database = FlowManager.getDatabase(getDatabaseName());
        return true;
    }

    protected abstract String getDatabaseName();


}
