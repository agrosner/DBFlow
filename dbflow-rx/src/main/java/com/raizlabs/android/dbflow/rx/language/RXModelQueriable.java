package com.raizlabs.android.dbflow.rx.language;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.List;

import rx.Single;
import rx.Single;

/**
 * Description: Mirrors {@link ModelQueriable} with RX constructs.
 */
public interface RXModelQueriable<TModel> extends RXQueriable {

    Single<CursorResult<TModel>> queryResults();

    Single<List<TModel>> queryList();

    Single<List<TModel>> queryList(DatabaseWrapper wrapper);

    /**
     * @return Single model, the first of potentially many results
     */
    Single<TModel> querySingle();

    /**
     * Allows you to specify a DB, useful for migrations.
     *
     * @return Single model, the first of potentially many results
     */
    Single<TModel> querySingle(DatabaseWrapper wrapper);

    /**
     * @return the table that this query comes from.
     */
    Class<TModel> getTable();

    /**
     * @return A cursor-backed list that handles conversion, retrieval, and caching of lists. Can
     * cache models dynamically by setting {@link FlowCursorList#setCacheModels(boolean)} to true.
     */
    Single<FlowCursorList<TModel>> cursorList();

    /**
     * @return A cursor-backed {@link List} that handles conversion, retrieval, caching, content changes,
     * and more.
     */
    Single<FlowQueryList<TModel>> flowQueryList();

    /**
     * Returns a {@link List} based on the custom {@link TQueryModel} you pass in.
     *
     * @param queryModelClass The query model class to use.
     * @param <TQueryModel>   The class that extends {@link BaseQueryModel}
     * @return A list of custom models that are not tied to a table.
     */
    <TQueryModel> Single<List<TQueryModel>> queryCustomList(Class<TQueryModel> queryModelClass);

    /**
     * Returns a single {@link TQueryModel} from this query.
     *
     * @param queryModelClass The class to use.
     * @param <TQueryModel>   The class that extends {@link BaseQueryModel}
     * @return A single model from the query.
     */
    <TQueryModel> Single<TQueryModel> queryCustomSingle(Class<TQueryModel> queryModelClass);

    /**
     * Disables caching on this query for the object retrieved from DB (if caching enabled). If
     * caching is not enabled, this method is ignored. This also disables caching in a {@link FlowCursorList}
     * or {@link FlowQueryList} if you {@link #flowQueryList()} or {@link #cursorList()}
     */
    RXModelQueriable<TModel> disableCaching();
}