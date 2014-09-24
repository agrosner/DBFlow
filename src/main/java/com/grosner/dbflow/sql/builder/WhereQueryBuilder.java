package com.grosner.dbflow.sql.builder;

import android.database.DatabaseUtils;

import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.converter.TypeConverter;
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
    public static class WhereParam {

        /**
         * The operation such as "=", "<", and more
         */
        private String mOperation;

        /**
         * The value of the column we care about
         */
        private Object mValue;

        /**
         * The column name
         */
        private String mColumn;

        public static WhereParam column(String columnName) {
            return new WhereParam(columnName);
        }

        /**
         * Creates a new instance
         *
         * @param columnName The name of the column in the DB
         */
        WhereParam(String columnName) {
            mColumn = columnName;
        }

        /**
         * Assigns the operation to "="
         *
         * @param value The value of the column in the DB in String value
         * @return
         */
        public WhereParam is(Object value) {
            mOperation = "=";
            return value(value);
        }

        /**
         * Assigns operation to ">"
         *
         * @param value The value of the column in the DB in String value
         * @return
         */
        public WhereParam greaterThan(Object value) {
            mOperation = ">";
            return value(value);
        }

        /**
         * Assigns operation to "<"
         *
         * @param value The value of the column in the DB in String value
         * @return
         */
        public WhereParam lessThan(Object value) {
            mOperation = "<";
            return value(value);
        }

        /**
         * Add a custom operation to this argument
         *
         * @param operation
         * @return
         */
        public WhereParam operation(String operation) {
            mOperation = operation;
            return this;
        }

        /**
         * The string value of the parameter
         *
         * @param value
         * @return
         */
        public WhereParam value(Object value) {
            mValue = value;
            return this;
        }

        /**
         * Returns the operation of it
         *
         * @return
         */
        public String operation() {
            return mOperation;
        }

        /**
         * Returns the value of the arg
         *
         * @return
         */
        public Object value() {
            return mValue;
        }

        /**
         * Returns the column name
         *
         * @return
         */
        public String columnName() {
            return mColumn;
        }
    }

    /**
     * Default empty param that will be replaced with the actual value when we call {@link #replaceEmptyParams(Object[])}
     */
    private static final String EMPTY_PARAM = "?";

    /**
     * The structure of the ModelClass this query pertains to
     */
    private TableStructure<ModelClass> mTableStructure;

    /**
     * The parameters to build this query with
     */
    private LinkedHashMap<String, WhereParam> mParams = new LinkedHashMap<String, WhereParam>();

    /**
     * Whether there is a new param, we will rebuild the query.
     */
    private boolean isChanged = false;

    /**
     * if true, all params must be empty. We will throw an exception if this is not the case.
     */
    private boolean useEmptyParams = false;

    /**
     * Constructs an instance of this class with the shared {@link com.grosner.dbflow.config.FlowManager}
     * and {@link ModelClass}.
     *
     * @param tableClass
     */
    public WhereQueryBuilder(Class<ModelClass> tableClass) {
        mTableStructure = FlowManager.getInstance().getTableStructureForClass(tableClass);
    }

    /**
     * Constructs an instance of this class with the specified {@link com.grosner.dbflow.config.FlowManager}
     * and {@link ModelClass}.
     *
     * @param flowManager
     * @param tableClass
     */
    public WhereQueryBuilder(FlowManager flowManager, Class<ModelClass> tableClass) {
        mTableStructure = flowManager.getTableStructureForClass(tableClass);
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

    /**
     * This will append all the primary key names with empty params. Ex: name = ?, columnName = ?
     *
     * @return
     */
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
    public WhereQueryBuilder<ModelClass> params(Map<String, WhereParam> params) {
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
        return param(WhereParam.column(columnName).operation(operator).value(value));

    }

    /**
     * Appends a param to this map. It will take the value and see if a {@link com.grosner.dbflow.converter.TypeConverter}
     * exists for the field. If so, we convert it to the database value. Also if the value is a string, we escape the string.
     *
     * @param whereParam The where arguments. We can specify other operators than just "="
     * @return
     */
    public WhereQueryBuilder<ModelClass> param(WhereParam whereParam) {
        mParams.put(whereParam.columnName(), whereParam);
        isChanged = true;
        return this;
    }

    /**
     * Appends a bunch of fields to the params list. It will retrieve the proper column name for the passed fields.
     *
     * @param fields The list of fields that we look up the column names for
     * @param values The values of the fields we wish to query for. Must match the length of fields keys
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
     * @param whereParam The value of the column we are looking for
     * @return
     */
    WhereQueryBuilder<ModelClass> appendParam(WhereParam whereParam) {
        return append(whereParam.columnName()).appendSpaceSeparated(whereParam.operation())
                .append(convertValueToString(whereParam.columnName(), whereParam.value()));
    }

    /**
     * Converts the given value for the column
     *
     * @param columnName The name of the column in the DB
     * @param value      The value of the column we are looking for
     * @return
     */
    protected String convertValueToString(String columnName, Object value) {
        String stringVal;
        if (!useEmptyParams) {
            final TypeConverter typeConverter = mTableStructure.getManager()
                    .getTypeConverterForClass(mTableStructure.getField(columnName).getType());
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
                appendParam(mParams.get(key));
                if (count < keys.size() - 1) {
                    appendSpaceSeparated("AND");
                }
                count++;
            }
        }

        return mQuery.toString();
    }

    /**
     * Replaces empty parameter values such as "columnName = ?" with the array of values passed in. It must
     * match the count of columns that are in this where query.
     *
     * @param values The values of the fields we wish to replace. Must match the length of the empty params and must be in empty param mode.
     * @return The query with the paramters filled in.
     */
    public WhereQueryBuilder<ModelClass> replaceEmptyParams(Object... values) {
        if (!useEmptyParams) {
            throw new IllegalStateException("The " + WhereQueryBuilder.class.getSimpleName() + " is " +
                    "not operating in empty param mode.");
        }
        if (mParams.size() != values.length) {
            throw new IllegalArgumentException("The count of values MUST match the number of columns they correspond to for " +
                    mTableStructure.getTableName());
        }

        WhereQueryBuilder<ModelClass> whereQueryBuilder =
                new WhereQueryBuilder<ModelClass>(mTableStructure.getManager(), mTableStructure.getModelType());
        Set<String> columnNames = mParams.keySet();

        int count = 0;
        for (String columnName : columnNames) {
            whereQueryBuilder.param(columnName, values[count]);
            count++;
        }

        return whereQueryBuilder;
    }

    /**
     * Builds a {@link com.grosner.dbflow.structure.Model} where query with its primary keys. The existing must
     * be based off the primary keys of the model.
     *
     * @param existing The existing where query we wish to generate new one from the model.
     * @param model    The existing model with all of its primary keys filled in
     * @return
     */
    public static <ModelClass extends Model> String getPrimaryModelWhere(WhereQueryBuilder<ModelClass> existing, ModelClass model) {
        return getModelBackedWhere(existing, existing.mTableStructure.getPrimaryKeys(), model);
    }

    /**
     * Returns a where query String from the existing builder and collection of fields.
     *
     * @param existing     The existing where query we wish to generate new one from the model.
     * @param fields       The list of fields that we look up the column names for
     * @param model        The model to get the field values from
     * @param <ModelClass>
     * @return
     */
    public static <ModelClass extends Model> String getModelBackedWhere(WhereQueryBuilder<ModelClass> existing,
                                                                        Collection<Field> fields, ModelClass model) {
        String query = existing.getQuery();
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

    /**
     * Returns the table structure that this {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder} uses.
     *
     * @return
     */
    public TableStructure<ModelClass> getTableStructure() {
        return mTableStructure;
    }

    /**
     * Returns the {@link ModelClass} that this query belongs to
     *
     * @return
     */
    public Class<ModelClass> getTableClass() {
        return getTableStructure().getModelType();
    }


}
