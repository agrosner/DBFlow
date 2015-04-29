package com.raizlabs.android.dbflow.structure.listener;

import android.content.ContentValues;

/**
 * Description: Called after the declared {@link android.content.ContentValues} are binded. It enables
 * us to listen and add custom behavior to the {@link android.content.ContentValues}. These must be
 * defined in a {@link com.raizlabs.android.dbflow.structure.Model} class to register properly.
 */
public interface ContentValuesListener {

    /**
     * Called during an {@link Model#update()} and at the end of
     * {@link com.raizlabs.android.dbflow.structure.ModelAdapter#bindToContentValues(android.content.ContentValues, com.raizlabs.android.dbflow.structure.Model)}
     * . It enables you to customly change the values as necessary during update to the database.
     *
     * @param contentValues The content values to
     */
    void onBindToContentValues(ContentValues contentValues);
}
