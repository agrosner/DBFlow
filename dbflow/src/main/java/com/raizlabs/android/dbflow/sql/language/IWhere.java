package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;

import java.util.List;

/**
 * Description:
 */

public interface IWhere<TModel> extends Transformable<TModel>, Query {

    @NonNull
    IWhere<TModel> and(SQLCondition condition);

    @NonNull
    IWhere<TModel> or(SQLCondition condition);

    @NonNull
    IWhere<TModel> andAll(List<SQLCondition> conditions);

    @NonNull
    IWhere<TModel> andAll(SQLCondition... conditions);

    @NonNull
    IWhere<TModel> exists(@NonNull IWhere where);
}
