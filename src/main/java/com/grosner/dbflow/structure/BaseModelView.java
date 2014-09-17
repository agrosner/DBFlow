package com.grosner.dbflow.structure;

import com.grosner.dbflow.sql.Where;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a base implementation for a {@link com.grosner.dbflow.structure.ModelView}.
 */
public abstract class BaseModelView<ModelClass extends Model> implements ModelView<ModelClass> {

    @Override
    public abstract Where<ModelClass> getWhere();

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
