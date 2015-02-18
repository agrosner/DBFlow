package com.raizlabs.android.dbflow.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.List;

/**
 * Description: Provides handy wrapper mechanisms for {@link android.content.ContentProvider}
 */
public class ContentUtils {

    /**
     * Inserts the model into the {@link android.content.ContentResolver}. Uses the insertUri to resolve
     * the reference and the model to convert its data into {@link android.content.ContentValues}
     *
     * @param insertUri    A {@link android.net.Uri} from the {@link com.raizlabs.android.dbflow.annotation.provider.ContentProvider} class definition.
     * @param model        The model to insert.
     * @param <TableClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return The Uri of the inserted data.
     */
    @SuppressWarnings("unchecked")
    public static <TableClass extends Model> Uri insert(Uri insertUri, TableClass model) {
        ModelAdapter<TableClass> adapter = (ModelAdapter<TableClass>) FlowManager.getModelAdapter(model.getClass());
        ContentValues contentValues = new ContentValues();
        adapter.bindToContentValues(contentValues, model);
        Uri uri = FlowManager.getContext().getContentResolver().insert(insertUri, contentValues);
        adapter.updateAutoIncrement(model, Long.valueOf(uri.getPathSegments().get(uri.getPathSegments().size() - 1)));
        return uri;
    }

    /**
     * Updates the model through the {@link android.content.ContentResolver}. Uses the updateUri to
     * resolve the reference and the model to convert its data in {@link android.content.ContentValues}
     *
     * @param updateUri    A {@link android.net.Uri} from the {@link com.raizlabs.android.dbflow.annotation.provider.ContentProvider}
     * @param model        The model to update
     * @param <TableClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return The number of rows updated.
     */
    @SuppressWarnings("unchecked")
    public static <TableClass extends Model> int update(Uri updateUri, TableClass model) {
        ModelAdapter<TableClass> adapter = (ModelAdapter<TableClass>) FlowManager.getModelAdapter(model.getClass());
        ContentValues contentValues = new ContentValues();
        adapter.bindToContentValues(contentValues, model);
        return FlowManager.getContext().getContentResolver().update(updateUri, contentValues, null, null);
    }

    /**
     * Deletes the specified model through the {@link android.content.ContentResolver}. Uses the deleteUri
     * to resolve the reference and the model to {@link com.raizlabs.android.dbflow.structure.ModelAdapter#getPrimaryModelWhere(com.raizlabs.android.dbflow.structure.Model)}
     *
     * @param deleteUri    A {@link android.net.Uri} from the {@link com.raizlabs.android.dbflow.annotation.provider.ContentProvider}
     * @param model        The model to delete
     * @param <TableClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return The number of rows deleted.
     */
    @SuppressWarnings("unchecked")
    public static <TableClass extends Model> int delete(Uri deleteUri, TableClass model) {
        ModelAdapter<TableClass> adapter = (ModelAdapter<TableClass>) FlowManager.getModelAdapter(model.getClass());
        return FlowManager.getContext().getContentResolver().delete(deleteUri, adapter.getPrimaryModelWhere(model).getQuery(), null);
    }

    /**
     * Queries the {@link android.content.ContentResolver} with the specified queryUri. It will generate
     * the correct query and return a list of {@link TableClass}
     *
     * @param queryUri        The URI of the query
     * @param table           The table to get from.
     * @param whereConditions The set of {@link com.raizlabs.android.dbflow.sql.builder.Condition} to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @param <TableClass>    The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return A list of {@link TableClass}
     */
    public static <TableClass extends Model> List<TableClass> queryList(Uri queryUri, Class<TableClass> table,
                                                                        ConditionQueryBuilder<TableClass> whereConditions,
                                                                        String orderBy, String... columns) {
        Cursor cursor = FlowManager.getContext().getContentResolver().query(queryUri, columns, whereConditions.getQuery(), null, orderBy);
        return SqlUtils.convertToList(table, cursor);
    }

    /**
     * Queries the {@link android.content.ContentResolver} with the specified queryUri. It will generate
     * the correct query and return a the first item from the list of {@link TableClass}
     *
     * @param queryUri        The URI of the query
     * @param table           The table to get from
     * @param whereConditions The set of {@link com.raizlabs.android.dbflow.sql.builder.Condition} to query the content provider.
     * @param orderBy         The order by clause without the ORDER BY
     * @param columns         The list of columns to query.
     * @param <TableClass>    The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return The first {@link TableClass} of the list query from the content provider.
     */
    public static <TableClass extends Model> TableClass querySingle(Uri queryUri, Class<TableClass> table,
                                                                    ConditionQueryBuilder<TableClass> whereConditions,
                                                                    String orderBy, String... columns) {
        List<TableClass> list = queryList(queryUri, table, whereConditions, orderBy, columns);
        return list.size() > 0 ? list.get(0) : null;
    }
}
