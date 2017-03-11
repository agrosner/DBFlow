package com.raizlabs.android.dbflow.rx.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.IJoin;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;

public class RXJoin<TModel, TFromModel> implements IJoin<TModel, TFromModel> {

    private final Join<TModel, TFromModel> innerJoin;
    private final RXFrom<TFromModel> from;

    public RXJoin(RXFrom<TFromModel> from, Class<TModel> table, @NonNull Join.JoinType joinType) {
        this.from = from;
        innerJoin = new Join<>(from.getInnerFrom(), table, joinType);
    }

    public RXJoin(RXFrom<TFromModel> from, @NonNull Join.JoinType joinType,
                  ModelQueriable<TModel> modelQueriable) {
        this.from = from;
        innerJoin = new Join<>(from.getInnerFrom(), joinType, modelQueriable);
    }

    @Override
    public String getQuery() {
        return innerJoin.getQuery();
    }

    @Override
    public RXJoin<TModel, TFromModel> as(String alias) {
        innerJoin.as(alias);
        return this;
    }

    @Override
    public RXFrom<TFromModel> natural() {
        innerJoin.natural();
        return from;
    }

    @Override
    public RXFrom<TFromModel> on(SQLCondition... onConditions) {
        innerJoin.on(onConditions);
        return from;
    }

    @Override
    public RXFrom<TFromModel> using(IProperty... columns) {
        innerJoin.using(columns);
        return from;
    }
}
