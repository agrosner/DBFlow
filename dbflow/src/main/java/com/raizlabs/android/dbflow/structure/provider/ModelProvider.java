package com.raizlabs.android.dbflow.structure.provider;

import android.content.ContentResolver;
import android.net.Uri;

import com.raizlabs.android.dbflow.sql.language.Operator;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;

/**
 * Description: A base interface for Models that are connected to providers.
 */
public interface ModelProvider {

    /**
     * Queries the {@link ContentResolver} of the app based on the passed parameters and
     * populates this object with the first row from the returned data.
     *
     * @param whereOperatorGroup The set of {@link Operator} to filter the query by.
     * @param orderBy             The order by without the ORDER BY
     * @param columns             The list of columns to select. Leave blank for *
     */
    void load(OperatorGroup whereOperatorGroup,
              String orderBy, String... columns);

    /**
     * Queries the {@link ContentResolver} of the app based on the primary keys of the object and populates
     * this object with the first row from the returned data.
     */
    void load();

    /**
     * @return The {@link android.net.Uri} that passes to a {@link android.content.ContentProvider} to delete a Model.
     */
    Uri getDeleteUri();

    /**
     * @return The {@link android.net.Uri} that passes to a {@link android.content.ContentProvider} to insert a Model.
     */
    Uri getInsertUri();

    /**
     * @return The {@link android.net.Uri} that passes to a {@link android.content.ContentProvider} to update a Model.
     */
    Uri getUpdateUri();

    /**
     * @return The {@link android.net.Uri} that passes to a {@link android.content.ContentProvider} to query a Model.
     */
    Uri getQueryUri();
}
