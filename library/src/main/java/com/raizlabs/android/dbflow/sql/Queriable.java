package com.raizlabs.android.dbflow.sql;

import android.database.Cursor;

import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: An interface for query objects to enable you to query from the database in a structured way.
 */
public interface Queriable<ModelClass extends Model> {

    /**
     * @return a cursor from the DB based on this query
     */
    public Cursor query();

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    public void queryClose();

    /**
     * @return a list of model converted items
     */
    public List<ModelClass> queryList();

    /**
     * @return Single model, the first of potentially many results
     */
    public ModelClass querySingle();

    /**
     * @return the table that this query comes from.
     */
    public Class<ModelClass> getTable();
}
