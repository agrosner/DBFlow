package com.raizlabs.android.dbflow.sql.language;

import android.content.ContentValues;

import com.raizlabs.android.dbflow.sql.queriable.Queriable;

/**
 * Description:
 */

public interface ISet<TModel> extends WhereBase<TModel>, Queriable, Transformable<TModel> {

    /**
     * Specifies a varg of conditions to append to this SET
     *
     * @param conditions The varg of conditions
     * @return This instance.
     */
    ISet<TModel> conditions(SQLCondition... conditions);

    /**
     * Specifies a set of content values to append to this SET as Conditions
     *
     * @param contentValues The set of values to append.
     * @return This instance.
     */
    ISet<TModel> conditionValues(ContentValues contentValues);

    /**
     * Begins completing the rest of this SET statement.
     *
     * @param conditions The conditions to fill the WHERE with.
     * @return The where piece of this query.
     */
    IWhere<TModel> where(SQLCondition... conditions);
}
