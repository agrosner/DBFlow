package com.raizlabs.android.dbflow.rx.language;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.ISet;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Set;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

public class RXSet<T> extends BaseRXQueriable<T> implements ISet<T> {

    private final Set<T> innerSet;

    public RXSet(Query updateQuery, Class<T> table) {
        super(table);
        innerSet = new Set<>(updateQuery, table);
    }

    @Override
    protected Queriable getInnerQueriable() {
        return innerSet;
    }

    @Override
    public String getQuery() {
        return innerSet.getQuery();
    }

    @Override
    public Class<T> getTable() {
        return innerSet.getTable();
    }

    @Override
    public Query getQueryBuilderBase() {
        return innerSet.getQueryBuilderBase();
    }

    @Override
    public RXWhere<T> groupBy(NameAlias... nameAliases) {
        return where().groupBy(nameAliases);
    }

    @Override
    public RXWhere<T> groupBy(IProperty... properties) {
        return where().groupBy(properties);
    }

    @Override
    public RXWhere<T> orderBy(NameAlias nameAlias, boolean ascending) {
        return where().orderBy(nameAlias, ascending);
    }

    @Override
    public RXWhere<T> orderBy(IProperty property, boolean ascending) {
        return where().orderBy(property, ascending);
    }

    @Override
    public RXWhere<T> orderBy(OrderBy orderBy) {
        return where().orderBy(orderBy);
    }

    @Override
    public RXWhere<T> limit(int count) {
        return where().limit(count);
    }

    @Override
    public RXWhere<T> offset(int offset) {
        return where().offset(offset);
    }

    @Override
    public RXWhere<T> having(SQLCondition... conditions) {
        return where().having(conditions);
    }

    @Override
    public RXWhere<T> orderByAll(List<OrderBy> orderBies) {
        return where().orderByAll(orderBies);
    }

    @NonNull
    @Override
    public RXSet<T> conditions(SQLCondition... conditions) {
        innerSet.conditions(conditions);
        return this;
    }

    @NonNull
    @Override
    public RXSet<T> conditionValues(ContentValues contentValues) {
        innerSet.conditionValues(contentValues);
        return this;
    }

    @NonNull
    @Override
    public RXWhere<T> where(SQLCondition... conditions) {
        return new RXWhere<>(this, conditions);
    }


    @Override
    public BaseModel.Action getPrimaryAction() {
        return innerSet.getPrimaryAction();
    }
}
