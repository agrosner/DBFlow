package com.dbflow5.provider

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.contentprovider.annotation.ContentProvider
import com.dbflow5.config.FlowLog
import com.dbflow5.config.modelAdapter
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.Operator
import com.dbflow5.query.OperatorGroup

/**
 * Description: Provides handy wrapper mechanisms for [android.content.ContentProvider]
 */
object ContentUtils {

    /**
     * The default content URI that Android recommends. Not necessary, however.
     */
    const val BASE_CONTENT_URI = "content://"

    /**
     * Constructs an Uri with the [.BASE_CONTENT_URI] and authority. Add paths to append to the Uri.
     *
     * @param authority The authority for a [ContentProvider]
     * @param paths     The list of paths to append.
     * @return A complete Uri for a [ContentProvider]
     */
    @JvmStatic
    fun buildUriWithAuthority(authority: String, vararg paths: String): Uri =
            buildUri(BASE_CONTENT_URI, authority, *paths)

    /**
     * Constructs an Uri with the specified baseContent uri and authority. Add paths to append to the Uri.
     *
     * @param baseContentUri The base content URI for a [ContentProvider]
     * @param authority      The authority for a [ContentProvider]
     * @param paths          The list of paths to append.
     * @return A complete Uri for a [ContentProvider]
     */
    @JvmStatic
    fun buildUri(baseContentUri: String, authority: String, vararg paths: String): Uri {
        val builder = Uri.parse(baseContentUri + authority).buildUpon()
        for (path in paths) {
            builder.appendPath(path)
        }
        return builder.build()
    }

    /**
     * Inserts the model into the [android.content.ContentResolver]. Uses the insertUri to resolve
     * the reference and the model to convert its data into [android.content.ContentValues]
     *
     * @param insertUri A [android.net.Uri] from the [ContentProvider] class definition.
     * @param model     The model to insert.
     * @return A Uri of the inserted data.
     */
    @JvmStatic
    fun <TableClass : Any> insert(context: Context, insertUri: Uri, model: TableClass): Uri? =
            insert(context.contentResolver, insertUri, model)

    /**
     * Inserts the model into the [android.content.ContentResolver]. Uses the insertUri to resolve
     * the reference and the model to convert its data into [android.content.ContentValues]
     *
     * @param contentResolver The content resolver to use
     * @param insertUri       A [android.net.Uri] from the [ContentProvider] class definition.
     * @param model           The model to insert.
     * @return The Uri of the inserted data.
     */
    @JvmStatic
    fun <TableClass : Any> insert(contentResolver: ContentResolver, insertUri: Uri, model: TableClass): Uri? {
        val adapter = model.javaClass.modelAdapter

        val contentValues = ContentValues()
        adapter.bindToInsertValues(contentValues, model)
        val uri: Uri? = contentResolver.insert(insertUri, contentValues)
        uri?.let {
            adapter.updateAutoIncrement(model, uri.pathSegments[uri.pathSegments.size - 1].toLong())
        }
        return uri
    }

    /**
     * Inserts the list of model into the [ContentResolver]. Binds all of the models to [ContentValues]
     * and runs the [ContentResolver.bulkInsert] method. Note: if any of these use
     * autoIncrementing primary keys the ROWID will not be properly updated from this method. If you care
     * use [.insert] instead.
     *
     * @param contentResolver The content resolver to use
     * @param bulkInsertUri   The URI to bulk insert with
     * @param table           The table to insert into
     * @param models          The models to insert.
     * @return The count of the rows affected by the insert.
     */
    @JvmStatic
    fun <TableClass : Any> bulkInsert(contentResolver: ContentResolver, bulkInsertUri: Uri,
                                      table: Class<TableClass>, models: List<TableClass>?): Int {
        val contentValues = arrayListOf<ContentValues>()
        val adapter = table.modelAdapter

        if (models != null) {
            for (i in contentValues.indices) {
                val values = ContentValues()
                adapter.bindToInsertValues(values, models[i])
                contentValues += values
            }
        }
        return contentResolver.bulkInsert(bulkInsertUri, contentValues.toTypedArray())
    }

    /**
     * Inserts the list of model into the [ContentResolver]. Binds all of the models to [ContentValues]
     * and runs the [ContentResolver.bulkInsert] method. Note: if any of these use
     * autoincrement primary keys the ROWID will not be properly updated from this method. If you care
     * use [.insert] instead.
     *
     * @param bulkInsertUri The URI to bulk insert with
     * @param table         The table to insert into
     * @param models        The models to insert.
     * @return The count of the rows affected by the insert.
     */
    @JvmStatic
    fun <TableClass : Any> bulkInsert(context: Context,
                                      bulkInsertUri: Uri,
                                      table: Class<TableClass>,
                                      models: List<TableClass>): Int =
            bulkInsert(context.contentResolver, bulkInsertUri, table, models)

    /**
     * Updates the model through the [android.content.ContentResolver]. Uses the updateUri to
     * resolve the reference and the model to convert its data in [android.content.ContentValues]
     *
     * @param updateUri A [android.net.Uri] from the [ContentProvider]
     * @param model     A model to update
     * @return The number of rows updated.
     */
    @JvmStatic
    fun <TableClass : Any> update(context: Context,
                                  updateUri: Uri,
                                  model: TableClass): Int =
            update(context.contentResolver, updateUri, model)

    /**
     * Updates the model through the [android.content.ContentResolver]. Uses the updateUri to
     * resolve the reference and the model to convert its data in [android.content.ContentValues]
     *
     * @param contentResolver The content resolver to use
     * @param updateUri       A [android.net.Uri] from the [ContentProvider]
     * @param model           The model to update
     * @return The number of rows updated.
     */
    @JvmStatic
    fun <TableClass : Any> update(contentResolver: ContentResolver,
                                  updateUri: Uri, model: TableClass): Int {
        val adapter = model.javaClass.modelAdapter

        val contentValues = ContentValues()
        adapter.bindToContentValues(contentValues, model)
        val count = contentResolver.update(updateUri, contentValues,
            adapter.getPrimaryConditionClause(model).query, null)
        if (count == 0) {
            FlowLog.log(FlowLog.Level.W, "Updated failed of: " + model.javaClass)
        }
        return count
    }

    /**
     * Deletes the specified model through the [android.content.ContentResolver]. Uses the deleteUri
     * to resolve the reference and the model to [ModelAdapter.getPrimaryConditionClause]}
     *
     * @param deleteUri A [android.net.Uri] from the [ContentProvider]
     * @param model     The model to delete
     * @return The number of rows deleted.
     */
    @JvmStatic
    fun <TableClass : Any> delete(context: Context, deleteUri: Uri, model: TableClass): Int =
            delete(context.contentResolver, deleteUri, model)

    /**
     * Deletes the specified model through the [android.content.ContentResolver]. Uses the deleteUri
     * to resolve the reference and the model to [ModelAdapter.getPrimaryConditionClause]
     *
     * @param contentResolver The content resolver to use
     * @param deleteUri       A [android.net.Uri] from the [ContentProvider]
     * @param model           The model to delete
     * @return The number of rows deleted.
     */
    @JvmStatic
    fun <TableClass : Any> delete(contentResolver: ContentResolver, deleteUri: Uri, model: TableClass): Int {
        val adapter = model.javaClass.modelAdapter

        val count = contentResolver.delete(deleteUri, adapter.getPrimaryConditionClause(model).query, null)

        // reset autoincrement to 0
        if (count > 0) {
            adapter.updateAutoIncrement(model, 0)
        } else {
            FlowLog.log(FlowLog.Level.W, "A delete on ${model.javaClass} within the ContentResolver appeared to fail.")
        }
        return count
    }

    /**
     * Queries the [android.content.ContentResolver] with the specified query uri. It generates
     * the correct query and returns a [android.database.Cursor]
     *
     * @param contentResolver The content resolver to use
     * @param queryUri        The URI of the query
     * @param whereConditions The set of [Operator] to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @return A [android.database.Cursor]
     */
    @JvmStatic
    fun query(contentResolver: ContentResolver, queryUri: Uri,
              whereConditions: OperatorGroup,
              orderBy: String?, vararg columns: String?): FlowCursor? =
        FlowCursor.from(contentResolver.query(queryUri, columns, whereConditions.query, null, orderBy))

    /**
     * Queries the [android.content.ContentResolver] with the specified queryUri. It will generate
     * the correct query and return a list of [TableClass]
     *
     * @param queryUri        The URI of the query
     * @param table           The table to get from.
     * @param whereConditions The set of [Operator] to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @return A list of [TableClass]
     */
    @JvmStatic
    fun <TableClass : Any> queryList(context: Context,
                                     queryUri: Uri, table: Class<TableClass>,
                                     databaseWrapper: DatabaseWrapper,
                                     whereConditions: OperatorGroup,
                                     orderBy: String, vararg columns: String): List<TableClass>? =
            queryList(context.contentResolver, queryUri, table,
                    databaseWrapper, whereConditions, orderBy, *columns)


    /**
     * Queries the [android.content.ContentResolver] with the specified queryUri. It will generate
     * the correct query and return a list of [TableClass]
     *
     * @param contentResolver The content resolver to use
     * @param queryUri        The URI of the query
     * @param table           The table to get from.
     * @param whereConditions The set of [Operator] to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @return A list of [TableClass]
     */
    @JvmStatic
    fun <TableClass : Any> queryList(contentResolver: ContentResolver, queryUri: Uri,
                                     table: Class<TableClass>,
                                     databaseWrapper: DatabaseWrapper,
                                     whereConditions: OperatorGroup,
                                     orderBy: String, vararg columns: String): List<TableClass>? {
        val cursor = FlowCursor.from(contentResolver.query(queryUri, columns, whereConditions.query, null, orderBy))
        return table.modelAdapter
            .listModelLoader
            .load(cursor, databaseWrapper)
    }

    /**
     * Queries the [android.content.ContentResolver] with the specified queryUri. It will generate
     * the correct query and return a the first item from the list of [TableClass]
     *
     * @param queryUri        The URI of the query
     * @param table           The table to get from
     * @param whereConditions The set of [Operator] to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @return The first [TableClass] of the list query from the content provider.
     */
    @JvmStatic
    fun <TableClass : Any> querySingle(context: Context,
                                       queryUri: Uri, table: Class<TableClass>,
                                       databaseWrapper: DatabaseWrapper,
                                       whereConditions: OperatorGroup,
                                       orderBy: String, vararg columns: String): TableClass? =
            querySingle(context.contentResolver, queryUri, table,
                    databaseWrapper, whereConditions, orderBy, *columns)

    /**
     * Queries the [android.content.ContentResolver] with the specified queryUri. It will generate
     * the correct query and return a the first item from the list of [TableClass]
     *
     * @param contentResolver The content resolver to use
     * @param queryUri        The URI of the query
     * @param table           The table to get from
     * @param whereConditions The set of [Operator] to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @return The first [TableClass] of the list query from the content provider.
     */
    @JvmStatic
    fun <TableClass : Any> querySingle(contentResolver: ContentResolver,
                                       queryUri: Uri, table: Class<TableClass>,
                                       databaseWrapper: DatabaseWrapper,
                                       whereConditions: OperatorGroup,
                                       orderBy: String, vararg columns: String): TableClass? {
        val list = queryList(contentResolver, queryUri, table,
                databaseWrapper, whereConditions, orderBy, *columns)
        return list?.let { if (list.isNotEmpty()) list[0] else null }
    }

}
