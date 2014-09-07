package com.raizlabs.android.dbflow.sql.builder;

import android.database.DatabaseUtils;

import com.raizlabs.android.dbflow.config.FlowManager;
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

    private Class<ModelClass> mModelClass;

    private TableStructure<ModelClass> mTableStructure;

    private List<String> mFieldNames;

    public AbstractWhereQueryBuilder(Class<ModelClass> tableClass) {

        mModelClass = tableClass;
        mTableStructure = FlowManager.getCache().getStructure().getTableStructureForClass(tableClass);

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
    public String getWhereQueryForModel(String[] fieldValues) {
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
}
