package com.raizlabs.android.dbflow.structure.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Provides handy wrapper mechanisms for {@link android.content.ContentProvider}
 */
public class ContentUtils {

    /**
     * The default content URI that Android recommends. Not necessary, however.
     */
    public static final String BASE_CONTENT_URI = "content://";

    /**
     * Constructs an Uri with the {@link #BASE_CONTENT_URI} and authority. Add paths to append to the Uri.
     *
     * @param authority The authority for a {@link ContentProvider}
     * @param paths     The list of paths to append.
     * @return A complete Uri for a {@link ContentProvider}
     */
    public static Uri buildUriWithAuthority(String authority, String... paths) {
        return buildUri(BASE_CONTENT_URI, authority, paths);
    }

    /**
     * Constructs an Uri with the specified basecontent uri and authority. Add paths to append to the Uri.
     *
     * @param baseContentUri The base content URI for a {@link ContentProvider}
     * @param authority      The authority for a {@link ContentProvider}
     * @param paths          The list of paths to append.
     * @return A complete Uri for a {@link ContentProvider}
     */
    public static Uri buildUri(String baseContentUri, String authority, String... paths) {
        Uri.Builder builder = Uri.parse(baseContentUri + authority).buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    /**
     * Inserts the model into the {@link android.content.ContentResolver}. Uses the insertUri to resolve
     * the reference and the model to convert its data into {@link android.content.ContentValues}
     *
     * @param insertUri    A {@link android.net.Uri} from the {@link ContentProvider} class definition.
     * @param model        The model to insert.
     * @param <TableClass> The class that implemets {@link Model}
     * @return A Uri of the inserted data.
     */
    public static <TableClass > Uri insert(Uri insertUri, TableClass model) {
        return insert(FlowManager.getContext().getContentResolver(), insertUri, model);
    }

    /**
     * Inserts the model into the {@link android.content.ContentResolver}. Uses the insertUri to resolve
     * the reference and the model to convert its data into {@link android.content.ContentValues}
     *
     * @param contentResolver The content resolver to use (if different from {@link FlowManager#getContext()})
     * @param insertUri       A {@link android.net.Uri} from the {@link ContentProvider} class definition.
     * @param model           The model to insert.
     * @param <TableClass>    The class that implements {@link Model}
     * @return The Uri of the inserted data.
     */
    @SuppressWarnings("unchecked")
    public static <TableClass > Uri insert(ContentResolver contentResolver, Uri insertUri, TableClass model) {
        ModelAdapter<TableClass> adapter = (ModelAdapter<TableClass>) FlowManager.getModelAdapter(model.getClass());

        ContentValues contentValues = new ContentValues();
        adapter.bindToInsertValues(contentValues, model);
        Uri uri = contentResolver.insert(insertUri, contentValues);
        adapter.updateAutoIncrement(model, Long.valueOf(uri.getPathSegments().get(uri.getPathSegments().size() - 1)));
        return uri;
    }

    /**
     * Inserts the list of model into the {@link ContentResolver}. Binds all of the models to {@link ContentValues}
     * and runs the {@link ContentResolver#bulkInsert(Uri, ContentValues[])} method. Note: if any of these use
     * autoincrementing primary keys the ROWID will not be properly updated from this method. If you care
     * use {@link #insert(ContentResolver, Uri, Model)} instead.
     *
     * @param contentResolver The content resolver to use (if different from {@link FlowManager#getContext()})
     * @param bulkInsertUri   The URI to bulk insert with
     * @param table           The table to insert into
     * @param models          The models to insert.
     * @param <TableClass>    The class that implements {@link Model}
     * @return The count of the rows affected by the insert.
     */
    public static <TableClass > int bulkInsert(ContentResolver contentResolver, Uri bulkInsertUri, Class<TableClass> table, List<TableClass> models) {
        ContentValues[] contentValues = new ContentValues[models == null ? 0 : models.size()];
        ModelAdapter<TableClass> adapter = FlowManager.getModelAdapter(table);

        if (models != null) {
            for (int i = 0; i < contentValues.length; i++) {
                contentValues[i] = new ContentValues();
                adapter.bindToInsertValues(contentValues[i], models.get(i));
            }
        }
        return contentResolver.bulkInsert(bulkInsertUri, contentValues);
    }

    /**
     * Inserts the list of model into the {@link ContentResolver}. Binds all of the models to {@link ContentValues}
     * and runs the {@link ContentResolver#bulkInsert(Uri, ContentValues[])} method. Note: if any of these use
     * autoincrementing primary keys the ROWID will not be properly updated from this method. If you care
     * use {@link #insert(Uri, Model)} instead.
     *
     * @param bulkInsertUri The URI to bulk insert with
     * @param table         The table to insert into
     * @param models        The models to insert.
     * @param <TableClass>  The class that implements {@link Model}
     * @return The count of the rows affected by the insert.
     */
    public static <TableClass > int bulkInsert(Uri bulkInsertUri, Class<TableClass> table, List<TableClass> models) {
        return bulkInsert(FlowManager.getContext().getContentResolver(), bulkInsertUri, table, models);
    }

    /**
     * Updates the model through the {@link android.content.ContentResolver}. Uses the updateUri to
     * resolve the reference and the model to convert its data in {@link android.content.ContentValues}
     *
     * @param updateUri    A {@link android.net.Uri} from the {@link ContentProvider}
     * @param model        A model to update
     * @param <TableClass> The class that implements {@link Model}
     * @return The number of rows updated.
     */
    public static <TableClass > int update(Uri updateUri, TableClass model) {
        return update(FlowManager.getContext().getContentResolver(), updateUri, model);
    }

    /**
     * Updates the model through the {@link android.content.ContentResolver}. Uses the updateUri to
     * resolve the reference and the model to convert its data in {@link android.content.ContentValues}
     *
     * @param contentResolver The content resolver to use (if different from {@link FlowManager#getContext()})
     * @param updateUri       A {@link android.net.Uri} from the {@link ContentProvider}
     * @param model           The model to update
     * @param <TableClass>    The class that implements {@link Model}
     * @return The number of rows updated.
     */
    @SuppressWarnings("unchecked")
    public static <TableClass > int update(ContentResolver contentResolver, Uri updateUri, TableClass model) {
        ModelAdapter<TableClass> adapter = (ModelAdapter<TableClass>) FlowManager.getModelAdapter(model.getClass());

        ContentValues contentValues = new ContentValues();
        adapter.bindToContentValues(contentValues, model);
        int count = contentResolver.update(updateUri, contentValues, adapter.getPrimaryConditionClause(model).getQuery(), null);
        if (count == 0) {
            FlowLog.log(FlowLog.Level.W, "Updated failed of: " + model.getClass());
        }
        return count;
    }

    /**
     * Deletes the specified model through the {@link android.content.ContentResolver}. Uses the deleteUri
     * to resolve the reference and the model to {@link ModelAdapter#getPrimaryConditionClause(Model)}
     *
     * @param deleteUri    A {@link android.net.Uri} from the {@link ContentProvider}
     * @param model        The model to delete
     * @param <TableClass> The class that implements {@link Model}
     * @return The number of rows deleted.
     */
    @SuppressWarnings("unchecked")
    public static <TableClass > int delete(Uri deleteUri, TableClass model) {
        return delete(FlowManager.getContext().getContentResolver(), deleteUri, model);
    }

    /**
     * Deletes the specified model through the {@link android.content.ContentResolver}. Uses the deleteUri
     * to resolve the reference and the model to {@link ModelAdapter#getPrimaryConditionClause(Model)}
     *
     * @param contentResolver The content resolver to use (if different from {@link FlowManager#getContext()})
     * @param deleteUri       A {@link android.net.Uri} from the {@link ContentProvider}
     * @param model           The model to delete
     * @param <TableClass>    The class that implements {@link Model}
     * @return The number of rows deleted.
     */
    @SuppressWarnings("unchecked")
    public static <TableClass > int delete(ContentResolver contentResolver, Uri deleteUri, TableClass model) {
        ModelAdapter<TableClass> adapter = (ModelAdapter<TableClass>) FlowManager.getModelAdapter(model.getClass());

        int count = contentResolver.delete(deleteUri, adapter.getPrimaryConditionClause(model).getQuery(), null);

        // reset autoincrement to 0
        if (count > 0) {
            adapter.updateAutoIncrement(model, 0);
        } else {
            FlowLog.log(FlowLog.Level.W, "A delete on " + model.getClass() + " within the ContentResolver appeared to fail.");
        }
        return count;
    }

    /**
     * Queries the {@link android.content.ContentResolver} with the specified query uri. It generates
     * the correct query and returns a {@link android.database.Cursor}
     *
     * @param contentResolver The content resolver to use (if different from {@link FlowManager#getContext()})
     * @param queryUri        The URI of the query
     * @param whereConditions The set of {@link Condition} to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @return A {@link android.database.Cursor}
     */
    public static Cursor query(ContentResolver contentResolver, Uri queryUri,
                               ConditionGroup whereConditions,
                               String orderBy, String... columns) {
        return contentResolver.query(queryUri, columns, whereConditions.getQuery(), null, orderBy);
    }

    /**
     * Queries the {@link android.content.ContentResolver} with the specified queryUri. It will generate
     * the correct query and return a list of {@link TableClass}
     *
     * @param queryUri        The URI of the query
     * @param table           The table to get from.
     * @param whereConditions The set of {@link Condition} to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @param <TableClass>    The class that implements {@link Model}
     * @return A list of {@link TableClass}
     */
    public static <TableClass > List<TableClass> queryList(Uri queryUri, Class<TableClass> table,
                                                                        ConditionGroup whereConditions,
                                                                        String orderBy, String... columns) {
        return queryList(FlowManager.getContext().getContentResolver(), queryUri, table, whereConditions, orderBy, columns);
    }


    /**
     * Queries the {@link android.content.ContentResolver} with the specified queryUri. It will generate
     * the correct query and return a list of {@link TableClass}
     *
     * @param contentResolver The content resolver to use (if different from {@link FlowManager#getContext()})
     * @param queryUri        The URI of the query
     * @param table           The table to get from.
     * @param whereConditions The set of {@link Condition} to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @param <TableClass>    The class that implements {@link Model}
     * @return A list of {@link TableClass}
     */
    public static <TableClass > List<TableClass> queryList(ContentResolver contentResolver, Uri queryUri, Class<TableClass> table,
                                                                        ConditionGroup whereConditions,
                                                                        String orderBy, String... columns) {
        Cursor cursor = contentResolver.query(queryUri, columns, whereConditions.getQuery(), null, orderBy);
        if (cursor != null) {
            return FlowManager.getModelAdapter(table)
                .getListModelLoader()
                .load(cursor);
        }

        return new ArrayList<>();
    }

    /**
     * Queries the {@link android.content.ContentResolver} with the specified queryUri. It will generate
     * the correct query and return a the first item from the list of {@link TableClass}
     *
     * @param queryUri        The URI of the query
     * @param table           The table to get from
     * @param whereConditions The set of {@link Condition} to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @param <TableClass>    The class that implements {@link Model}
     * @return The first {@link TableClass} of the list query from the content provider.
     */
    public static <TableClass > TableClass querySingle(Uri queryUri, Class<TableClass> table,
                                                                    ConditionGroup whereConditions,
                                                                    String orderBy, String... columns) {
        return querySingle(FlowManager.getContext().getContentResolver(), queryUri, table, whereConditions, orderBy, columns);
    }

    /**
     * Queries the {@link android.content.ContentResolver} with the specified queryUri. It will generate
     * the correct query and return a the first item from the list of {@link TableClass}
     *
     * @param contentResolver The content resolver to use (if different from {@link FlowManager#getContext()})
     * @param queryUri        The URI of the query
     * @param table           The table to get from
     * @param whereConditions The set of {@link Condition} to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @param <TableClass>    The class that implements {@link Model}
     * @return The first {@link TableClass} of the list query from the content provider.
     */
    public static <TableClass > TableClass querySingle(ContentResolver contentResolver, Uri queryUri, Class<TableClass> table,
                                                                    ConditionGroup whereConditions,
                                                                    String orderBy, String... columns) {
        List<TableClass> list = queryList(contentResolver, queryUri, table, whereConditions, orderBy, columns);
        return list.size() > 0 ? list.get(0) : null;
    }

}
