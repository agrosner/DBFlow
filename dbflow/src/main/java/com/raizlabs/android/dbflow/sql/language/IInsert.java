package com.raizlabs.android.dbflow.sql.language;

import android.content.ContentValues;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;

import java.util.List;

/**
 * Description:
 */

public interface IInsert<TModel> {

    IInsert<TModel> columns(String... columns);

    IInsert<TModel> columns(IProperty... properties);

    IInsert<TModel> columns(List<IProperty> properties);

    IInsert<TModel> asColumns();

    IInsert<TModel> values(Object... values);

    IInsert<TModel> columnValues(SQLCondition... conditions);

    IInsert<TModel> columnValues(ConditionGroup conditionGroup);

    IInsert<TModel> columnValues(ContentValues contentValues);

    IInsert<TModel> select(IFrom<?> selectFrom);

    IInsert<TModel> or(ConflictAction action);

    IInsert<TModel> orReplace();

    IInsert<TModel> orRollback();

    IInsert<TModel> orAbort();

    IInsert<TModel> orFail();

    IInsert<TModel> orIgnore();
}
