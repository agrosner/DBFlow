package com.raizlabs.android.dbflow.runtime;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.property.Property;

/**
 * Description: The base provider class that {@link com.raizlabs.android.dbflow.annotation.provider.ContentProvider}
 * extend when generated.
 */
public abstract class BaseContentProvider extends ContentProvider {

    /**
     * Converts the column into a {@link Property}. This exists since the propery method is static and cannot
     * be referenced easily.
     */
    public interface PropertyConverter {

        Property fromName(String columnName);
    }

    protected static Property[] toProperties(PropertyConverter propertyConverter, String... selection) {
        Property[] properties = new Property[selection.length];
        for (int i = 0; i < selection.length; i++) {
            String columnName = selection[i];
            properties[i] = propertyConverter.fromName(columnName);
        }
        return properties;
    }

    protected BaseDatabaseDefinition database;

    @Override
    public boolean onCreate() {
        database = FlowManager.getDatabase(getDatabaseName());
        return true;
    }

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

    protected abstract String getDatabaseName();

    protected abstract int bulkInsert(Uri uri, ContentValues contentValues);

}
