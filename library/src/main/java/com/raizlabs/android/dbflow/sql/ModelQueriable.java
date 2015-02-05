package com.raizlabs.android.dbflow.sql;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowTableList;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: An interface for query objects to enable you to query from the database in a structured way.
 */
public interface ModelQueriable<ModelClass extends Model> extends Queriable {

    /**
     * @return a list of model converted items
     */
    public List<ModelClass> queryList();

    /**
     * @return Single model, the first of potentially many results
     */
    public ModelClass querySingle();

    /**
     * @return the table that this query comes from.
     */
    public Class<ModelClass> getTable();

    /**
     * @return A cursor-backed list that handles conversion, retrieval, and caching of lists. Can
     * cache models dynamically by setting {@link com.raizlabs.android.dbflow.list.FlowCursorList#setCacheModels(boolean)} to true.
     */
    public FlowCursorList<ModelClass> queryCursorList();

    /**
     * @return A cursor-backed {@link java.util.List} that handles conversion, retrieval, caching, content changes,
     * and more.
     */
    public FlowTableList<ModelClass> queryTableList();
}