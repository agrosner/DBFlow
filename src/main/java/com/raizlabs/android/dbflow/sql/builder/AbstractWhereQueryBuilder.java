package com.raizlabs.android.dbflow.sql.builder;

import android.database.DatabaseUtils;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.TableStructure;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Builds the "where" primary key statement for a specific model class.
 */
public abstract class AbstractWhereQueryBuilder<ModelClass extends Model> extends QueryBuilder<AbstractWhereQueryBuilder> {

    private TableStructure<ModelClass> mTableStructure;

    private List<String> mFieldNames;

    public AbstractWhereQueryBuilder(Class<ModelClass> tableClass) {

        mTableStructure = FlowManager.getTableStructureForClass(tableClass);

        mFieldNames = getFieldNames(mTableStructure);

        int size = mFieldNames.size();
        for (int i = 0; i < size; i++) {
            append(mFieldNames.get(i));
            append("=?");

            if (i < size - 1) {
                appendSpaceSeparated("AND");
            }
        }
    }

    /**
     * Send back the field names you wish to retrieve.
     *
     * @param tableStructure
     * @return
     */
    protected abstract List<String> getFieldNames(TableStructure<ModelClass> tableStructure);

    /**
     * Replaces the query string question marks with the values from the passed keys.
     *
     * @param fieldValues the array of field values as strings
     * @return The full SQL query with the "?" filled in.
     */
    public String getWhereQueryForArgs(String[] fieldValues) {
        String query = getQuery();

        for (int i = 0; i < fieldValues.length; i++) {
            final Field field = mTableStructure.getField(mFieldNames.get(i));
            field.setAccessible(true);
            String value = fieldValues[i];
            try {
                if (String.class.isAssignableFrom(field.getType())) {
                    String escaped = DatabaseUtils.sqlEscapeString(value);
                    query = query.replaceFirst("\\?", escaped);
                } else {
                    query = query.replaceFirst("\\?", value);
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return query;
    }

    /**
     * Builds the "where" query section for the model with it's {@link com.raizlabs.android.dbflow.converter.TypeConverter}
     * values.
     * @param model
     * @return
     */
    public String getWhereQueryForModel(ModelClass model) {
        String sql = getQuery();

        int size = mFieldNames.size();
        for(int i = 0; i < size; i++){
            final Field field = mTableStructure.getField(mFieldNames.get(i));
            field.setAccessible(true);
            try {
                Object object = field.get(model);
                if(object==null){
                    throw new PrimaryKeyCannotBeNullException("The primary key: " + field.getName()
                            + "from " + mTableStructure.getTableName() + " cannot be null.");
                } else {
                    final TypeConverter typeConverter = FlowManager.getCache()
                            .getStructure().getTypeConverterForClass(field.getType());
                    if (typeConverter != null) {
                        // serialize data
                        object = typeConverter.getDBValue(object);
                        // set new object type
                        if (object != null) {
                            Class fieldType = object.getClass();
                            // check that the serializer returned what it promised
                            if (!fieldType.equals(typeConverter.getDatabaseType())) {
                                FlowLog.log(FlowLog.Level.W, String.format(TypeConverter.class.getSimpleName() + " returned wrong type: expected a %s but got a %s",
                                        typeConverter.getDatabaseType(), fieldType));
                            }
                        }
                    }

                    if (object instanceof Number) {
                        sql = sql.replaceFirst("\\?", object.toString());
                    } else {
                        String escaped = DatabaseUtils.sqlEscapeString(object.toString());

                        sql = sql.replaceFirst("\\?", escaped);
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return sql;
    }

    public List<String> getFieldNames() {
        return mFieldNames;
    }

    public TableStructure<ModelClass> getTableStructure() {
        return mTableStructure;
    }
}
