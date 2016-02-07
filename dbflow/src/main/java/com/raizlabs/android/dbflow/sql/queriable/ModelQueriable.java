package com.raizlabs.android.dbflow.sql.queriable;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.List;

/**
 * Description: An interface for query objects to enable you to query from the database in a structured way.
 * Examples of such statements are: {@link From}, {@link Where}, {@link StringQuery}
 */
public interface ModelQueriable<ModelClass extends Model> extends Queriable {

    /**
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The
     *                      values will be bound as Strings.
     * @return a list of model converted items
     */
    List<ModelClass> queryList(String... selectionArgs);

    /**
     * Allows you to specify a DB, useful for migrations.
     *
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The
     *                      values will be bound as Strings.
     * @return a list of model converted items
     */
    List<ModelClass> queryList(DatabaseWrapper wrapper, String... selectionArgs);

    /**
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The
     *                      values will be bound as Strings.
     * @return Single model, the first of potentially many results
     */
    ModelClass querySingle(String... selectionArgs);

    /**
     * Allows you to specify a DB, useful for migrations.
     *
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The
     *                      values will be bound as Strings.
     * @return Single model, the first of potentially many results
     */
    ModelClass querySingle(DatabaseWrapper wrapper, String... selectionArgs);

    /**
     * Queries and populates the specified {@link ModelContainer} from the database.
     *
     * @param instance              A non-null instance to populate from the DB.
     * @param selectionArgs         You may include ?s in selection, which will be replaced by the values
     *                              from selectionArgs, in order that they appear in the selection. The
     *                              values will be bound as Strings.
     * @param <ModelContainerClass> The kind of data that the instance provides.
     * @return The specified instance populated from the DB.
     */
    <ModelContainerClass extends ModelContainer<ModelClass, ?>> ModelContainerClass queryModelContainer(@NonNull ModelContainerClass instance, String... selectionArgs);

    /**
     * @return the table that this query comes from.
     */
    Class<ModelClass> getTable();

    /**
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The
     *                      values will be bound as Strings.
     * @return A cursor-backed list that handles conversion, retrieval, and caching of lists. Can
     * cache models dynamically by setting {@link com.raizlabs.android.dbflow.list.FlowCursorList#setCacheModels(boolean)} to true.
     */
    FlowCursorList<ModelClass> queryCursorList(String... selectionArgs);

    /**
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The
     *                      values will be bound as Strings.
     * @return A cursor-backed {@link java.util.List} that handles conversion, retrieval, caching, content changes,
     * and more.
     */
    FlowQueryList<ModelClass> queryTableList(String... selectionArgs);

    /**
     * @return an async version of this query to run.
     */
    AsyncQuery<ModelClass> async();

    /**
     * Returns a {@link List} based on the custom {@link QueryClass} you pass in.
     *
     * @param queryModelClass The query model class to use.
     * @param selectionArgs   You may include ?s in selection, which will be replaced by the values
     *                        from selectionArgs, in order that they appear in the selection. The
     *                        values will be bound as Strings.
     * @param <QueryClass>    The class that extends {@link BaseQueryModel}
     * @return A list of custom models that are not tied to a table.
     */
    <QueryClass extends BaseQueryModel> List<QueryClass> queryCustomList(Class<QueryClass> queryModelClass, String... selectionArgs);

    /**
     * Returns a single {@link QueryClass} from this query.
     *
     * @param queryModelClass The class to use.
     * @param selectionArgs   You may include ?s in selection, which will be replaced by the values
     *                        from selectionArgs, in order that they appear in the selection. The
     *                        values will be bound as Strings.
     * @param <QueryClass>    The class that extends {@link BaseQueryModel}
     * @return A single model from the query.
     */
    <QueryClass extends BaseQueryModel> QueryClass queryCustomSingle(Class<QueryClass> queryModelClass, String... selectionArgs);
}