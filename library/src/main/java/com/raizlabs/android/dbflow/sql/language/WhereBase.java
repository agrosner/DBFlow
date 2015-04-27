package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Description: The base
 */
public interface WhereBase<ModelClass extends Model> extends Query {

    Class<ModelClass> getTable();

    Query getQueryBuilderBase();
}
