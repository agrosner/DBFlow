package com.grosner.dbflow.converter;

import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This interface describes how a foreign key gets converted into the DB.
 */
public interface ForeignKeyConverter<ModelClass extends Model> {

    public Class<ModelClass> getModelClass();

    /**
     * Returns the Database value for this class. This is what will be stored in the referent class' table.
     * @return
     */
    public String getDBValue(ModelClass modelClass);

    /**
     * Returns the foreign key values from the DB so we can retrieve this object.
     * @param dbValue
     * @return
     */
    public Object[] getForeignKeys(String dbValue);
}
