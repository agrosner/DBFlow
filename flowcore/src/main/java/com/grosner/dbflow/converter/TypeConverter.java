package com.grosner.dbflow.converter;

/**
 * Author: andrewgrosner
 * Description: This class is responsible for converting the stored database value into the field value in
 * a {@link com.grosner.dbflow.structure.Model}
 */
@com.grosner.dbflow.annotation.TypeConverter
public abstract class TypeConverter<DataClass, ModelClass> {

    /**
     * Converts the {@link ModelClass} into a {@link DataClass}
     *
     * @param model this will be called upon syncing
     * @return The {@link DataClass} value that converts into a SQLite type
     */
    public abstract DataClass getDBValue(ModelClass model);

    /**
     * Converts a {@link DataClass} from the DB into a {@link ModelClass}/
     *
     * @param data This will be called when the model is loaded from the DB
     * @return The {@link ModelClass} value that gets set in a Model that holds the data class.
     */
    public abstract ModelClass getModelValue(DataClass data);
}
