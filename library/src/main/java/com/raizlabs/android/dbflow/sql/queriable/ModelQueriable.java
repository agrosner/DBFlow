package com.raizlabs.android.dbflow.sql.queriable;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: An interface for query objects to enable you to query from the database in a structured way.
 * Examples of such statements are: {@link From}, {@link Where}, {@link StringQuery}
 */
public interface ModelQueriable<ModelClass extends Model> extends Queriable {

    /**
     * @return a list of model converted items
     */
    List<ModelClass> queryList();

    /**
     * @return Single model, the first of potentially many results
     */
    ModelClass querySingle();

    /**
     * @return the table that this query comes from.
     */
    Class<ModelClass> getTable();

    /**
     * @return A cursor-backed list that handles conversion, retrieval, and caching of lists. Can
     * cache models dynamically by setting {@link com.raizlabs.android.dbflow.list.FlowCursorList#setCacheModels(boolean)} to true.
     */
    FlowCursorList<ModelClass> queryCursorList();

    /**
     * @return A cursor-backed {@link java.util.List} that handles conversion, retrieval, caching, content changes,
     * and more.
     */
    FlowQueryList<ModelClass> queryTableList();

    /**
     * @return an async version of this query to run.
     */
    AsyncQuery<ModelClass> async();

    /**
     * Returns a {@link List} based on the custom {@link QueryClass} you pass in.
     *
     * @param queryModelClass The query model class to use.
     * @param <QueryClass>    The class that extends {@link BaseQueryModel}
     * @return A list of custom models that are not tied to a table.
     */
    <QueryClass extends BaseQueryModel> List<QueryClass> queryCustomList(Class<QueryClass> queryModelClass);

    /**
     * Returns a single {@link QueryClass} from this query.
     *
     * @param queryModelClass The class to use.
     * @param <QueryClass>    The class that extends {@link BaseQueryModel}
     * @return A single model from the query.
     */
    <QueryClass extends BaseQueryModel> QueryClass queryCustomSingle(Class<QueryClass> queryModelClass);
}