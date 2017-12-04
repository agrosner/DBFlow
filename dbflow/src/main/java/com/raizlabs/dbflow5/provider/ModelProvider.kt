package com.raizlabs.dbflow5.provider

import android.content.ContentResolver
import android.net.Uri
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.Operator
import com.raizlabs.dbflow5.query.OperatorGroup

/**
 * Description: A base interface for Models that are connected to providers.
 */
interface ModelProvider {

    /**
     * @return The [android.net.Uri] that passes to a [android.content.ContentProvider] to delete a Model.
     */
    val deleteUri: Uri

    /**
     * @return The [android.net.Uri] that passes to a [android.content.ContentProvider] to insert a Model.
     */
    val insertUri: Uri

    /**
     * @return The [android.net.Uri] that passes to a [android.content.ContentProvider] to update a Model.
     */
    val updateUri: Uri

    /**
     * @return The [android.net.Uri] that passes to a [android.content.ContentProvider] to query a Model.
     */
    val queryUri: Uri

    /**
     * Queries the [ContentResolver] of the app based on the passed parameters and
     * populates this object with the first row from the returned data.
     *
     * @param whereOperatorGroup The set of [Operator] to filter the query by.
     * @param orderBy            The order by without the ORDER BY
     * @param columns            The list of columns to select. Leave blank for *
     */
    fun <T> load(whereOperatorGroup: OperatorGroup,
             orderBy: String?,
             wrapper: DatabaseWrapper,
             vararg columns: String?) : T?

    /**
     * Queries the [ContentResolver] of the app based on the primary keys of the object and returns a
     * new object with the first row from the returned data.
     */
    fun <T> load(wrapper: DatabaseWrapper): T?
}
