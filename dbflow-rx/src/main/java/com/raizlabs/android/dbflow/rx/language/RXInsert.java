package com.raizlabs.android.dbflow.rx.language;

import android.content.ContentValues;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.IFrom;
import com.raizlabs.android.dbflow.sql.language.IInsert;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.List;

import rx.Observable;

/**
 * Description:
 */

public class RXInsert<T> extends BaseRXQueriable<T> implements IInsert<T> {

    private final Insert<T> innerInsert;

    public RXInsert(Class<T> table) {
        super(table);
        innerInsert = new Insert<>(table);
    }

    @Override
    protected Queriable getInnerQueriable() {
        return innerInsert;
    }

    @Override
    public RXInsert<T> columns(String... columns) {
        innerInsert.columns(columns);
        return this;
    }

    @Override
    public RXInsert<T> columns(IProperty... properties) {
        innerInsert.columns(properties);
        return this;
    }

    @Override
    public RXInsert<T> columns(List<IProperty> properties) {
        innerInsert.columns(properties);
        return this;
    }

    @Override
    public RXInsert<T> asColumns() {
        innerInsert.asColumns();
        return this;
    }

    @Override
    public RXInsert<T> values(Object... values) {
        innerInsert.values(values);
        return this;
    }

    @Override
    public RXInsert<T> columnValues(SQLCondition... conditions) {
        innerInsert.columnValues(conditions);
        return this;
    }

    @Override
    public RXInsert<T> columnValues(ConditionGroup conditionGroup) {
        innerInsert.columnValues(conditionGroup);
        return this;
    }

    @Override
    public RXInsert<T> columnValues(ContentValues contentValues) {
        innerInsert.columnValues(contentValues);
        return this;
    }

    @Override
    public RXInsert<T> select(IFrom<?> selectFrom) {
        innerInsert.select(selectFrom);
        return this;
    }

    @Override
    public RXInsert<T> or(ConflictAction action) {
        innerInsert.or(action);
        return this;
    }

    @Override
    public RXInsert<T> orReplace() {
        innerInsert.orReplace();
        return this;
    }

    @Override
    public RXInsert<T> orRollback() {
        innerInsert.orRollback();
        return this;
    }

    @Override
    public RXInsert<T> orAbort() {
        innerInsert.orAbort();
        return this;
    }

    @Override
    public RXInsert<T> orFail() {
        innerInsert.orFail();
        return this;
    }

    @Override
    public RXInsert<T> orIgnore() {
        innerInsert.orIgnore();
        return this;
    }

    @Override
    public Observable<Long> executeUpdateDelete(DatabaseWrapper databaseWrapper) {
        throw new IllegalStateException("Cannot call executeUpdateDelete() from an Insert");
    }

    @Override
    public Observable<Long> executeUpdateDelete() {
        throw new IllegalStateException("Cannot call executeUpdateDelete() from an Insert");
    }
}
