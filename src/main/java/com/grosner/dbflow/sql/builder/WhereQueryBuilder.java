package com.grosner.dbflow.sql.builder;

import android.database.DatabaseUtils;

import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.TableStructure;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Builds the "where" primary key statement for a specific model class.
 */
public class WhereQueryBuilder<ModelClass extends Model> extends QueryBuilder<WhereQueryBuilder<ModelClass>> {

    /**
     * Holds information related to each "where" piece
     */
    public static class WhereArgs {

        private final String mOperation;

        private final String mValue;

        public WhereArgs(String operation, String value) {
            mOperation = operation;
            mValue = value;
        }

        public String operation() {
            return mOperation;
        }

        public String value() {
            return mValue;
        }
    }


    private static final String EMPTY_PARAM = "?";

    /**
     * The structure of the ModelClass this query pertains to
     */
    private TableStructure<ModelClass> mTableStructure;

    /**
     * The parameters to build this query with
     */
    private LinkedHashMap<String, WhereArgs> mParams = new LinkedHashMap<String, WhereArgs>();

    /**
     * Whether there is a new param, we will rebuild the query.
     */
    private boolean isChanged = false;

    /**
     * if true, all params must be empty. We will throw an exception if this is not the case.
     */
    private boolean useEmptyParams = false;

    public WhereQueryBuilder(Class<ModelClass> tableClass) {
        mTableStructure = FlowManager.getTableStructureForClass(tableClass);
    }

    /**
     * Appends all primary field parameters with the specified value to this query statement.
     *
     * @param values The values of the primary keys we wish to query for. Must match the length of primary keys
     * @return
     */
    public WhereQueryBuilder<ModelClass> primaryParams(Object... values) {
        return appendFieldParams(mTableStructure.getPrimaryKeys(), values);
    }

    public WhereQueryBuilder<ModelClass> emptyPrimaryParams() {
        useEmptyParams = true;
        return appendFieldParams(mTableStructure.getPrimaryKeys());
    }

    /**
     * Appends all the parameters from the specified map
     *
     * @param params The mapping between column names and the string-represented value
     * @return
     */
    public WhereQueryBuilder<ModelClass> params(Map<String, WhereArgs> params) {
        mParams.putAll(params);
        isChanged = true;
        return this;
    }

    /**
     * Appends an empty param to this map that will be represented with a "?". All params must either be empty or not.
     *
     * @param columnName The name of the column in the DB
     * @return
     */
    public WhereQueryBuilder<ModelClass> emptyParam(String columnName) {
        useEmptyParams = true;
        return param(columnName, EMPTY_PARAM);
    }

    /**
     * Appends a param to this map. It will take the value and see if a {@link com.grosner.dbflow.converter.TypeConverter}
     * exists for the field. If so, we convert it to the database value. Also if the value is a string, we escape the string.
     * EX: columnName = value
     *
     * @param columnName The name of the column in the DB
     * @param value      The value of the column we are looking for
     * @return
     */
    public WhereQueryBuilder<ModelClass> param(String columnName, Object value) {
        return param(columnName, "=", value);
    }

    /**
     * Appends a param to this map. It will take the value and see if a {@link com.grosner.dbflow.converter.TypeConverter}
     * exists for the field. If so, we convert it to the database value. Also if the value is a string, we escape the string.
     *
     * @param columnName The name of the column in the DB
     * @param operator   The operator to use "=", "<", etc.
     * @param value      The value of the column we are looking for
     * @return
     */
    public WhereQueryBuilder<ModelClass> param(String columnName, String operator, Object value) {
        if (useEmptyParams && !EMPTY_PARAM.equals(value)) {
            throw new IllegalStateException("The " + WhereQueryBuilder.class.getSimpleName() + " is " +
                    "operating in empty param mode. All params must be empty");
        }

        mParams.put(columnName, new WhereArgs(operator, convertValueToString(columnName, value)));
        isChanged = true;
        return this;
    }

    /**
     * Appends a bunch of fields to the params list. It will retrieve the proper column name for the passed fields.
     *
     * @param fields
     * @param values
     * @return
     */
    WhereQueryBuilder<ModelClass> appendFieldParams(Collection<Field> fields, Object... values) {
        if (!useEmptyParams && fields.size() != values.length) {
            throw new IllegalArgumentException("The count of values MUST match the number of fields they correspond to for " +
                    mTableStructure.getTableName());
        } else if (useEmptyParams) {
            values = new Object[fields.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = EMPTY_PARAM;
            }
        }

        int count = 0;
        for (Field field : fields) {
            String fieldName = mTableStructure.getColumnName(field);
            Object value = values[count];
            param(fieldName, value);
            count++;
        }

        return this;
    }

    /**
     * Internal utility method for appending a where param
     *
     * @param columnName The name of the column in the DB
     * @param whereArgs  The value of the column we are looking for
     * @return
     */
    WhereQueryBuilder<ModelClass> appendParam(String columnName, WhereArgs whereArgs) {
        return append(columnName).appendSpaceSeparated(whereArgs.operation()).append(whereArgs.value());
    }

    /**
     * Converts the given value for the column
     *
     * @param columnName
     * @param value
     * @return
     */
    protected String convertValueToString(String columnName, Object value) {
        String stringVal;
        if (!useEmptyParams) {
            final TypeConverter typeConverter = FlowManager.getCache()
                    .getStructure().getTypeConverterForClass(mTableStructure.getField(columnName).getType());
            if (typeConverter != null) {
                // serialize data
                value = typeConverter.getDBValue(value);
                // set new object type
                if (value != null) {
                    Class fieldType = value.getClass();
                    // check that the serializer returned what it promised
                    if (!fieldType.equals(typeConverter.getDatabaseType())) {
                        FlowLog.log(FlowLog.Level.W, String.format(TypeConverter.class.getSimpleName() +
                                        " returned wrong type: expected a %s but got a %s",
                                typeConverter.getDatabaseType(), fieldType));
                    }
                }
            }
        }

        if (value instanceof Number) {
            stringVal = String.valueOf(value);
        } else {
            stringVal = String.valueOf(value);
            if (!stringVal.equals(EMPTY_PARAM)) {
                stringVal = DatabaseUtils.sqlEscapeString(stringVal);
            }
        }

        return stringVal;
    }

    @Override
    public String getQuery() {
        // Empty query, we will build it now with params, or if the query has changed.
        if (isChanged || mQuery.length() == 0) {
            isChanged = false;
            mQuery = new StringBuilder();

            Set<String> keys = mParams.keySet();
            int count = 0;
            for (String key : keys) {
                appendParam(key, mParams.get(key));
                if (count < keys.size() - 1) {
                    appendSpaceSeparated("AND");
                }
            }
        }

        return mQuery.toString();
    }

    public WhereQueryBuilder<ModelClass> replaceEmptyParams(Object[] values) {
        if (mParams.size() != values.length) {
            throw new IllegalArgumentException("The count of values MUST match the number of columns they correspond to for " +
                    mTableStructure.getTableName());
        }

        WhereQueryBuilder<ModelClass> whereQueryBuilder = new WhereQueryBuilder<ModelClass>(mTableStructure.getModelType());
        Set<String> columnNames = mParams.keySet();

        int count = 0;
        for (String columnName : columnNames) {
            whereQueryBuilder.param(columnName, values[count]);
            count++;
        }

        return whereQueryBuilder;
    }

    /**
     * Builds the "where" query section for the model with it's {@link com.grosner.dbflow.converter.TypeConverter}
     * values.
     *
     * @param model
     * @return
     */
    public static <ModelClass extends Model> String getPrimaryModelWhere(WhereQueryBuilder<ModelClass> existing, ModelClass model) {
        return getModelBackedWhere(existing, existing.mTableStructure.getPrimaryKeys(), model);
    }

    public static <ModelClass extends Model> String getModelBackedWhere(WhereQueryBuilder<ModelClass> existing,
                                                                        Collection<Field> fields, ModelClass model) {
        String query = existing.getQuery();
        int size = fields.size();
        for (Field primaryField : fields) {
            String columnName = existing.mTableStructure.getColumnName(primaryField);
            primaryField.setAccessible(true);
            try {
                Object object = primaryField.get(model);
                if (object == null) {
                    throw new PrimaryKeyCannotBeNullException("The primary key: " + primaryField.getName()
                            + "from " + existing.mTableStructure.getTableName() + " cannot be null.");
                } else {
                    query = query.replaceFirst("\\?", existing.convertValueToString(columnName, object));
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return query;
    }

    public TableStructure<ModelClass> getTableStructure() {
        return mTableStructure;
    }
}
