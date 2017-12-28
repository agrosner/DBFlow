package com.raizlabs.android.dbflow.structure.listener;

import android.content.ContentValues;

import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Description: Called after the declared {@link ContentValues} are binded. It enables
 * us to listen and add custom behavior to the {@link ContentValues}. These must be
 * defined in a {@link Model} class to register properly.
 * <p>
 * This class will no longer get called during updates unless explicit call to
 * {@link ModelAdapter#bindToContentValues(ContentValues, Object)}
 * or {@link ModelAdapter#bindToInsertValues(ContentValues, Object)}
 *
 * @see SQLiteStatementListener
 */
@Deprecated
public interface ContentValuesListener {

    /**
     * Called during an {@link Model#update()} and at the end of
     * {@link ModelAdapter#bindToContentValues(ContentValues, Object)}
     * . It enables you to customly change the values as necessary during update to the database.
     *
     * @param contentValues The content values to bind to for an update.
     */
    void onBindToContentValues(ContentValues contentValues);

    /**
     * Called during an {@link Model#update()} and at the end of
     * {@link ModelAdapter#bindToInsertValues(ContentValues, Object)}.
     * It enables you to customly change the values as necessary during insert
     * to the database for a {@link ContentProvider}.
     *
     * @param contentValues The content values to insert into DB for a {@link ContentProvider}
     */
    void onBindToInsertValues(ContentValues contentValues);
}
