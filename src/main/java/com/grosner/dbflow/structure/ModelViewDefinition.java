package com.grosner.dbflow.structure;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.Where;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Describes the SQL query for a view. It is a virtual table that we can query from.
 */
public abstract class ModelViewDefinition<ModelViewClass extends BaseModelView<ModelClass>, ModelClass extends Model> {

    protected FlowManager mManager;

    public ModelViewDefinition(FlowManager flowManager) {
        mManager = flowManager;
    }

    /**
     * Returns the {@link com.grosner.dbflow.sql.Where} query that creates this class
     *
     * @return The creation query for creating this view.
     */
    public abstract Where<ModelClass> getWhere();

    /**
     * Returns the name of the View in the DB
     *
     * @return
     */
    public abstract String getName();

    public abstract Class<ModelViewClass> getModelViewClass();

    public abstract Class<ModelClass> getModelClass();

}
