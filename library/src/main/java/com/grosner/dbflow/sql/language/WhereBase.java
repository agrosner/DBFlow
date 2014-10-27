package com.grosner.dbflow.sql.language;

import com.grosner.dbflow.sql.Query;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Description: The base
 */
public interface WhereBase<ModelClass extends Model> extends Query {

    public Class<ModelClass> getTable();

    public Query getQueryBuilderBase();
}
