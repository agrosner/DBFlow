package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;

import java.util.List;

/**
 * Description:
 */

public interface IWhere<TModel> extends Transformable<TModel>, Query {

    IWhere<TModel> and(SQLCondition condition);

    IWhere<TModel> or(SQLCondition condition);

    IWhere<TModel> andAll(List<SQLCondition> conditions);

    IWhere<TModel> andAll(SQLCondition... conditions);

    IWhere<TModel> exists(@NonNull IWhere where);
}
