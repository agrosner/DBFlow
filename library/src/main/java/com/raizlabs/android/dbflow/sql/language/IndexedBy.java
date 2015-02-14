package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The INDEXED BY part of a SELECT/UPDATE/DELETE
 */
public class IndexedBy<ModelClass extends Model> implements WhereBase<ModelClass> {

    private final String mIndexName;

    private final WhereBase<ModelClass> mWhereBase;

    /**
     * Creates the INDEXED BY part of the clause.
     *
     * @param mIndexName The name of the index
     * @param whereBase  The base piece of this query
     */
    IndexedBy(@NonNull String mIndexName, WhereBase<ModelClass> whereBase) {
        this.mIndexName = mIndexName;
        this.mWhereBase = whereBase;
    }

    /**
     * @return a WHERE piece of this query
     */
    public Where<ModelClass> where() {
        return new Where<>(this);
    }

    /**
     * @param where The string part of where
     * @return a WHERE query with the specified string
     */
    public Where<ModelClass> where(String where) {
        return where().whereClause(where);
    }

    /**
     * @param conditionQueryBuilder The set of conditions used to build a WHERE query.
     * @return a WHERE query with the specified {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     */
    public Where<ModelClass> where(ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        return where().whereQuery(conditionQueryBuilder);
    }

    /**
     * @param conditions the list of conditions used to build a WHERE query.
     * @return a WHERE query with the specified {@link com.raizlabs.android.dbflow.sql.builder.Condition}
     */
    public Where<ModelClass> where(Condition... conditions) {
        return where().andThese(conditions);
    }

    @Override
    public Class<ModelClass> getTable() {
        return mWhereBase.getTable();
    }

    @Override
    public Query getQueryBuilderBase() {
        return mWhereBase.getQueryBuilderBase();
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(mWhereBase.getQuery())
                .append("INDEXED BY ").appendQuoted(mIndexName).appendSpace();
        return queryBuilder.getQuery();
    }
}
