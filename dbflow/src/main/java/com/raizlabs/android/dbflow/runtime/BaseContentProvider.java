package com.raizlabs.android.dbflow.runtime;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
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

        IProperty fromName(String columnName);
    }

    protected static IProperty[] toProperties(PropertyConverter propertyConverter, String... selection) {
        IProperty[] properties = new IProperty[selection.length];
        for (int i = 0; i < selection.length; i++) {
            String columnName = selection[i];
            String[] query = columnName.split("=");
            if (query.length > 1) {
                properties[i] = propertyConverter.fromName(query[0]);
            }
        }
        return properties;
    }

    protected BaseDatabaseDefinition database;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public int bulkInsert(@NonNull final Uri uri, @NonNull final ContentValues[] values) {
        final int[] count = {0};
        TransactionManager.transact(getDatabase().getWritableDatabase(), new Runnable() {
            @Override
            public void run() {
                for (ContentValues contentValues : values) {
                    count[0] += bulkInsert(uri, contentValues);
                }
            }
        });
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return count[0];
    }

    protected abstract String getDatabaseName();

    protected abstract int bulkInsert(Uri uri, ContentValues contentValues);

    protected BaseDatabaseDefinition getDatabase() {
        if (database == null) {
            database = FlowManager.getDatabase(getDatabaseName());
        }
        return database;
    }

}
