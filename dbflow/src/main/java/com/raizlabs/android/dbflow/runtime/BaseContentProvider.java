package com.raizlabs.android.dbflow.runtime;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Description: The base provider class that {@link com.raizlabs.android.dbflow.annotation.provider.ContentProvider}
 * extend when generated.
 */
public abstract class BaseContentProvider extends ContentProvider {

    protected BaseDatabaseDefinition database;

    @Override
    public boolean onCreate() {
        database = FlowManager.getDatabase(getDatabaseName());
        return true;
    }

    protected abstract String getDatabaseName();

    @Override
    public int bulkInsert(final Uri uri, final ContentValues[] values) {
        final int[] count = {0};
        TransactionManager.transact(database.getWritableDatabase(), new Runnable() {
            @Override
            public void run() {
                for (ContentValues contentValues : values) {
                    count[0] += bulkInsert(uri, contentValues);
                }
            }
        });
        getContext().getContentResolver().notifyChange(uri, null);
        return count[0];
    }

    protected abstract int bulkInsert(Uri uri, ContentValues contentValues);

}
