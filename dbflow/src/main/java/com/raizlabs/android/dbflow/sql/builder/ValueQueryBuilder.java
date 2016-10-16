package com.raizlabs.android.dbflow.sql.builder;

import android.database.DatabaseUtils;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Condition;

/**
 * Description: Provides conversion for model values into DB values for specific values as well as model-backed methods.
 */
public class ValueQueryBuilder extends QueryBuilder<ValueQueryBuilder> {

    /**
     * Returns a string containing the tokens converted into DBValues joined by delimiters.
     *
     * @param delimiter The text to join the text with.
     * @param tokens    an array objects to be joined. Strings will be formed from
     *                  the objects by calling object.toString().
     * @return A joined string
     */
    @SuppressWarnings("unchecked")
    public static String joinArguments(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(ValueQueryBuilder.convertValueToDatabaseString(token));
        }
        return sb.toString();
    }

    /**
     * Returns a string containing the tokens converted into DBValues joined by delimiters.
     *
     * @param delimiter The text to join the text with.
     * @param tokens    an array objects to be joined. Strings will be formed from
     *                  the objects by calling object.toString().
     * @return A joined string
     */
    @SuppressWarnings("unchecked")
    public static String joinArguments(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(ValueQueryBuilder.convertValueToDatabaseString(token));
        }
        return sb.toString();
    }

    /**
     * Converts the given value into a String for the Database using a {@link com.raizlabs.android.dbflow.converter.TypeConverter}
     * if one exists.
     *
     * @param modelValue The un-typeconverted value of the model.
     * @return The string DB value of the object
     */
    @SuppressWarnings("unchecked")
    public static String convertValueToDatabaseString(Object modelValue) {
        String stringVal;
        Object value = modelValue;
        if (value != null) {
            TypeConverter typeConverter = FlowManager.getTypeConverterForClass(value.getClass());
            if (typeConverter != null) {
                value = typeConverter.getDBValue(value);
            }
        }

        if (value instanceof Number) {
            stringVal = String.valueOf(value);
        } else {
            stringVal = String.valueOf(value);
            if (!stringVal.equals(Condition.Operation.EMPTY_PARAM)) {
                stringVal = DatabaseUtils.sqlEscapeString(stringVal);
            }
        }
        return stringVal;
    }

    public ValueQueryBuilder() {
    }

    public ValueQueryBuilder(String string) {
        super(string);
    }

    /**
     * Appends a Table name to this query from the Model specified.
     *
     * @param table
     * @return
     */
    public ValueQueryBuilder appendTableName(Class<?> table) {
        return append(FlowManager.getTableName(table));
    }

    /**
     * Appends a database value of the object
     *
     * @param modelValue
     * @return
     */
    @SuppressWarnings("unchecked")
    public ValueQueryBuilder appendDBValue(Object modelValue) {
        return append(convertValueToDatabaseString(modelValue));
    }

    /**
     * Appends a list of model object's that are converted into the proper DB values as a string
     * and separated by commas.
     *
     * @param modelList The list of objects that are converted into proper DB values
     * @return
     */
    public ValueQueryBuilder appendModelList(Iterable<?> modelList) {
        if (modelList != null) {
            append(joinArguments(",", modelList));
        }
        return this;
    }

    /**
     * Appends an array of model object's that are converted into the proper DB values as a string
     * and separated by commas.
     *
     * @param modelList The array of objects that are converted into proper DB values
     * @return
     */
    public ValueQueryBuilder appendModelArray(Object[] modelList) {
        if (modelList != null) {
            append(joinArguments(",", modelList));
        }
        return this;
    }

}
