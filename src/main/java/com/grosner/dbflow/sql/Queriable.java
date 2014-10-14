package com.grosner.dbflow.sql;

import android.database.Cursor;

import com.grosner.dbflow.structure.Model;

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
     * @return a list of model converted items
     */
    public List<ModelClass> queryList();

    /**
     * @return Single model, the first of potentially many results
     */
    public ModelClass querySingle();
}
