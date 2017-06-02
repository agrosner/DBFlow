package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.List;

/**
 * Description: An interface for query objects to enable you to query from the database in a structured way.
 * Examples of such statements are: {@link From}, {@link Where}, {@link StringQuery}
 */
public interface ModelQueriable<TModel> extends Queriable {

    /**
     * @return A wrapper class around {@link Cursor} that allows you to convert its data into results.
     */
    @NonNull
    CursorResult<TModel> queryResults();

    /**
     * @return a list of model converted items
     */
    @NonNull
    List<TModel> queryList();

    /**
     * Allows you to specify a DB, useful for migrations.
     *
     * @return a list of model converted items
     */
    @NonNull
    List<TModel> queryList(@NonNull DatabaseWrapper wrapper);

    /**
     * @return Single model, the first of potentially many results
     */
    @Nullable
    TModel querySingle();

    /**
     * Allows you to specify a DB, useful for migrations.
     *
     * @return Single model, the first of potentially many results
     */
    @Nullable
    TModel querySingle(@NonNull DatabaseWrapper wrapper);

    /**
     * @return the table that this query comes from.
     */
    @NonNull
    Class<TModel> getTable();

    /**
     * @return A cursor-backed list that handles conversion, retrieval, and caching of lists. Can
     * cache models dynamically by setting {@link FlowCursorList#setCacheModels(boolean)} to true.
     */
    @NonNull
    FlowCursorList<TModel> cursorList();

    /**
     * @return A cursor-backed {@link List} that handles conversion, retrieval, caching, content changes,
     * and more.
     */
    @NonNull
    FlowQueryList<TModel> flowQueryList();

    /**
     * @return an async version of this query to run.
     */
    @NonNull
    AsyncQuery<TModel> async();

    /**
     * Returns a {@link List} based on the custom {@link TQueryModel} you pass in.
     *
     * @param queryModelClass The query model class to use.
     * @param <TQueryModel>   The class that extends {@link BaseQueryModel}
     * @return A list of custom models that are not tied to a table.
     */
    @NonNull
    <TQueryModel> List<TQueryModel> queryCustomList(@NonNull Class<TQueryModel> queryModelClass);

    /**
     * Returns a single {@link TQueryModel} from this query.
     *
     * @param queryModelClass The class to use.
     * @param <TQueryModel>   The class that extends {@link BaseQueryModel}
     * @return A single model from the query.
     */
    @Nullable
    <TQueryModel> TQueryModel queryCustomSingle(@NonNull Class<TQueryModel> queryModelClass);

    /**
     * Disables caching on this query for the object retrieved from DB (if caching enabled). If
     * caching is not enabled, this method is ignored. This also disables caching in a {@link FlowCursorList}
     * or {@link FlowQueryList} if you {@link #flowQueryList()} or {@link #cursorList()}
     */
    @NonNull
    ModelQueriable<TModel> disableCaching();
}