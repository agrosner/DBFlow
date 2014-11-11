package com.grosner.dbflow.structure.container;

import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelAdapter;

/**
 * Author: andrewgrosner
 * Description: The primary interface for converting data that acts like a {@link com.grosner.dbflow.structure.Model} into a model object
 * that it corresponds to. It is also used in {@link com.grosner.dbflow.structure.Column#FOREIGN_KEY} to save
 * and retrieve values of objects.
 */
public interface ModelContainer<ModelClass extends Model, DataClass> extends Model {

    /**
     * Will convert the object into {@link ModelClass}
     *
     * @return The model from this json.
     */
    public ModelClass toModel();

    /**
     * @return The underlying data object that this container uses to imitate a model.
     */
    public DataClass getData();

    /**
     * @return New instance of underlying data for parsing
     */
    public DataClass newDataInstance();

    /**
     * Returns a new instance of this container with a different Model class
     * @param inValue
     * @param columnClass
     * @return
     */
    public BaseModelContainer getInstance(Object inValue, Class<? extends Model> columnClass);

    /**
     * Returns the value with the specified key
     *
     * @param columnName
     * @return
     */
    public Object getValue(String columnName);

    /**
     * Puts the value with the specified key and value to the object
     *
     * @param columnName
     * @param value
     */
    public void put(String columnName, Object value);

    /**
     * Returns the associated table structure for this container
     *
     * @return
     */
    public ModelAdapter<ModelClass> getModelAdapter();

    /**
     * Returns the table that's associated with this container
     *
     * @return
     */
    public Class<ModelClass> getTable();
}
