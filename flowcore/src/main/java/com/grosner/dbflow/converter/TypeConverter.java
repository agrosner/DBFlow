package com.grosner.dbflow.converter;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class is responsible for converting the store DB value into the field value in
 * a {@link com.grosner.dbflow.structure.Model}
 */
@com.grosner.dbflow.annotation.TypeConverter
public abstract class TypeConverter<DataClass, ModelClass> {

    /**
     * Converts the {@link ModelClass} into a {@link DataClass}
     *
     * @param model this will be called upon {@link com.grosner.dbflow.structure.Model#save(boolean)}
     * @return The {@link DataClass} value that converts into a SQLite type
     */
    public abstract DataClass getDBValue(ModelClass model);

    /**
     * Converts a {@link DataClass} from the DB into a {@link ModelClass}/
     *
     * @param data This will be called when the model is {@link com.grosner.dbflow.structure.Model#load(android.database.Cursor)}
     *             from the DB
     * @return The {@link ModelClass} value that gets set in a {@link com.grosner.dbflow.structure.Model} that holds this class.
     */
    public abstract ModelClass getModelValue(DataClass data);
}
