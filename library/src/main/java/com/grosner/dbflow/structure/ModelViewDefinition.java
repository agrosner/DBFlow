package com.grosner.dbflow.structure;

import com.grosner.dbflow.sql.language.Where;

/**
 * Author: andrewgrosner
 * Description: Describes the SQL query for a view. It is a virtual table that we can query from.
 */
public interface ModelViewDefinition<ModelViewClass extends BaseModelView<ModelClass>, ModelClass extends Model> {

    /**
     * Returns the {@link com.grosner.dbflow.sql.language.Where} query that creates this class
     *
     * @return The creation query for creating this view.
     */
    public Where<ModelClass> getWhere();

    /**
     * Returns the name of the View in the DB
     *
     * @return
     */
    public String getName();

    public Class<ModelViewClass> getModelViewClass();

    public Class<ModelClass> getModelClass();

}
