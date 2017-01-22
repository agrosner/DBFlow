package com.raizlabs.android.dbflow.rx.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.IWhere;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.language.WhereBase;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;

import java.util.List;

/**
 * Description:
 */
public class RXWhere<T> extends BaseRXModelQueriable<T> implements IWhere<T> {

    private final Where<T> innerWhere;

    public RXWhere(WhereBase<T> whereBase, SQLCondition... conditions) {
        super(whereBase.getTable());
        innerWhere = new Where<>(whereBase, conditions);
    }

    @Override
    protected BaseModelQueriable<T> getInnerModelQueriable() {
        return innerWhere;
    }

    @Override
    public RXWhere<T> and(SQLCondition condition) {
        innerWhere.and(condition);
        return this;
    }

    @Override
    public RXWhere<T> or(SQLCondition condition) {
        innerWhere.or(condition);
        return this;
    }

    @Override
    public RXWhere<T> andAll(List<SQLCondition> conditions) {
        innerWhere.andAll(conditions);
        return this;
    }

    @Override
    public RXWhere<T> andAll(SQLCondition... conditions) {
        innerWhere.andAll(conditions);
        return this;
    }

    @Override
    public RXWhere<T> exists(@NonNull IWhere where) {
        innerWhere.exists(where);
        return this;
    }

    @Override
    public RXWhere<T> groupBy(NameAlias... nameAliases) {
        innerWhere.groupBy(nameAliases);
        return this;
    }

    @Override
    public RXWhere<T> groupBy(IProperty... properties) {
        innerWhere.groupBy(properties);
        return this;
    }

    @Override
    public RXWhere<T> orderBy(NameAlias nameAlias, boolean ascending) {
        innerWhere.orderBy(nameAlias, ascending);
        return this;
    }

    @Override
    public RXWhere<T> orderBy(IProperty property, boolean ascending) {
        innerWhere.orderBy(property, ascending);
        return this;
    }

    @Override
    public RXWhere<T> orderBy(OrderBy orderBy) {
        innerWhere.orderBy(orderBy);
        return this;
    }

    @Override
    public RXWhere<T> limit(int count) {
        innerWhere.limit(count);
        return this;
    }

    @Override
    public RXWhere<T> offset(int offset) {
        innerWhere.offset(offset);
        return this;
    }

    @Override
    public RXWhere<T> having(SQLCondition... conditions) {
        innerWhere.having(conditions);
        return this;
    }

    @Override
    public RXWhere<T> orderByAll(List<OrderBy> orderBies) {
        innerWhere.orderByAll(orderBies);
        return this;
    }

}
