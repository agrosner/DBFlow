package com.grosner.dbflow.converter;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class is responsible for converting the store DB value into the field value in
 * a {@link com.grosner.dbflow.structure.Model}
 */
public interface TypeConverter<DataClass, ModelClass> {

    public Class<DataClass> getDatabaseType();

    public Class<ModelClass> getModelType();

    public DataClass getDBValue(ModelClass model);

    public ModelClass getModelValue(DataClass data);
}
