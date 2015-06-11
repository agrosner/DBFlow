package com.raizlabs.android.dbflow.structure.container;

import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Author: andrewgrosner
 * Description: The primary interface for converting data that acts like a {@link com.raizlabs.android.dbflow.structure.Model} into a model object
 * that it corresponds to. It is also used in {@link com.raizlabs.android.dbflow.structure.Column#FOREIGN_KEY} to save
 * and retrieve values of objects.
 */
public interface ModelContainer<ModelClass extends Model, DataClass> extends Model {

    /**
     * Will convert the object into {@link ModelClass}
     *
     * @return The model from this json.
     */
    ModelClass toModel();

    /**
     * @return The underlying data object that this container uses to imitate a model.
     */
    DataClass getData();

    /**
     * Changes the underlying data. This method should also invalidate the {@link ModelClass} in this container.
     *
     * @param data The data to back this object.
     */
    void setData(DataClass data);

    /**
     * @return New instance of underlying data for parsing
     */
    DataClass newDataInstance();

    /**
     * @param inValue     The data to construct a new instance from
     * @param columnClass The Model type
     * @return new instance of this container with a different Model class when we have
     * children who are {@link com.raizlabs.android.dbflow.structure.Model}
     */
    BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass);

    /**
     * @param columnName The name of the column
     * @return the value with the specified key
     */
    Object getValue(String columnName);

    /**
     * Puts the value with the specified key and value to the object
     *
     * @param columnName The name of the column
     * @param value      The value of the item in Model form.
     */
    void put(String columnName, Object value);

    /**
     * @return The associated model adapter from the table for this {@link com.raizlabs.android.dbflow.structure.container.ModelContainer}
     */
    ModelAdapter<ModelClass> getModelAdapter();

    /**
     * @return the table that's associated with this container
     */
    Class<ModelClass> getTable();
}
