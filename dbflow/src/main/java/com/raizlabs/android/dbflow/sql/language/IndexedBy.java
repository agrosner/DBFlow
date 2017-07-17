package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description: The INDEXED BY part of a SELECT/UPDATE/DELETE
 */
public class IndexedBy<TModel> extends BaseTransformable<TModel> {

    private final IndexProperty<TModel> indexProperty;

    private final WhereBase<TModel> whereBase;

    /**
     * Creates the INDEXED BY part of the clause.
     *
     * @param indexProperty The index property generated.
     * @param whereBase     The base piece of this query
     */
    public IndexedBy(IndexProperty<TModel> indexProperty, WhereBase<TModel> whereBase) {
        super(whereBase.getTable());
        this.indexProperty = indexProperty;
        this.whereBase = whereBase;
    }

    @NonNull
    @Override
    public Query getQueryBuilderBase() {
        return whereBase.getQueryBuilderBase();
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(whereBase.getQuery())
            .append(" INDEXED BY ").append(QueryBuilder.quoteIfNeeded(indexProperty.getIndexName())).appendSpace();
        return queryBuilder.getQuery();
    }

    @NonNull
    @Override
    public BaseModel.Action getPrimaryAction() {
        return whereBase.getPrimaryAction();
    }
}
