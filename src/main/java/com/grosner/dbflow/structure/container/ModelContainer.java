package com.grosner.dbflow.structure.container;

import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Description: The primary interface for converting data that acts like a {@link com.grosner.dbflow.structure.Model} into a model object
 * that it corresponds to.
 */
public interface ModelContainer<ModelClass extends Model> {

    /**
     * Will convert the object into {@link ModelClass}
     * @return The model from this json.
     */
    public ModelClass toModel();
}
