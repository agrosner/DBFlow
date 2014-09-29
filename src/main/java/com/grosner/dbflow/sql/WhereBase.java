package com.grosner.dbflow.sql;

import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface WhereBase<ModelClass extends Model> extends Query {

    public Class<ModelClass> getTable();

    public Query getQueryBuilderBase();
}
