package com.raizlabs.android.dbflow.sql.language;

import android.text.TextUtils;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: A SQL SELECT statement generator. It generates the SELECT part of the statement.
 */
public class Select implements Query {

    /**
     * Default does not include the qualifier
     */
    public static final int NONE = -1;
    /**
     * The select qualifier to append to the SELECT statement
     */
    private int mSelectQualifier = NONE;
    /**
     * SELECT DISTINCT call
     */
    public static final int DISTINCT = 0;
    /**
     * SELECT ALL call
     */
    public static final int ALL = 1;
    /**
     * SELECT method call
     */
    public static final int METHOD = 2;
    /**
     * Specifies the column names to select from
     */
    private final String[] mColumns;
    /**
     * The method name we wish to execute
     */
    private String mMethodName;

    /**
     * The column name passed into the method name
     */
    private String mColumnName;

    /**
     * Creates this instance with the specified columns from the specified {@link com.raizlabs.android.dbflow.config.FlowManager}
     *
     * @param columns
     */
    public Select(String... columns) {
        mColumns = columns;
    }

    /**
     * Selects a model by id
     *
     * @param tableClass   The table to select from
     * @param ids          The ids to use
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return
     */
    public static <ModelClass extends Model> ModelClass byId(Class<ModelClass> tableClass, Object... ids) {
        ConditionQueryBuilder<ModelClass> primaryQuery = FlowManager.getPrimaryWhereQuery(tableClass);
        return withCondition(primaryQuery.replaceEmptyParams(ids));
    }

    /**
     * Selects a single model object with the specified {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param conditionQueryBuilder The where query we will use
     * @param <ModelClass>          The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     * @return the first model from the database cursor.
     */
    public static <ModelClass extends Model> ModelClass withCondition(ConditionQueryBuilder<ModelClass> conditionQueryBuilder, String... columns) {
        return Where.with(conditionQueryBuilder, columns).querySingle();
    }

    /**
     * Selects all models from the table based on a set of {@link com.raizlabs.android.dbflow.sql.builder.Condition}.
     *
     * @param tableClass   The table to select the list from
     * @param conditions   The list of conditions to select the list of models from
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return the list of every row in the table
     */
    public static <ModelClass extends Model> List<ModelClass> all(Class<ModelClass> tableClass, Condition... conditions) {
        return new Select().from(tableClass).where(conditions).queryList();
    }

    /**
     * Retrieves the count of the {@link ModelClass} table based on the specified WHERE conditions.
     * @param tableClass The table to select count from
     * @param conditions The list of conditions to select the count of models from
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return The count of how many rows exist within the table based on the conditions passed.
     */
    public static <ModelClass extends Model> long count(Class<ModelClass> tableClass, Condition...conditions) {
        return new Select().count().from(tableClass).where(conditions).count();
    }

    /**
     * Passes this statement to the {@link From}
     *
     * @param table        The model table to run this query on
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return the From part of this query
     */
    public <ModelClass extends Model> From<ModelClass> from(Class<ModelClass> table) {
        return new From<ModelClass>(this, table);
    }

    /**
     * Selects a list of model objects with the specified {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param conditionQueryBuilder The where query we will use
     * @param <ModelClass>          The class that implements {@link com.raizlabs.android.dbflow.structure.Model}.
     * @return the list of models from the database cursor.
     */
    public static <ModelClass extends Model> List<ModelClass> all(ConditionQueryBuilder<ModelClass> conditionQueryBuilder, String... columns) {
        return Where.with(conditionQueryBuilder, columns).queryList();
    }

    /**
     * appends {@link #DISTINCT} to the query
     *
     * @return
     */
    public Select distinct() {
        return selectQualifier(DISTINCT);
    }

    /**
     * Helper method to pick the correct qualifier for a SELECT query
     *
     * @param qualifierInt Can be {@link #ALL}, {@link #NONE}, {@link #DISTINCT}, or {@link #COUNT}
     * @return
     */
    public Select selectQualifier(int qualifierInt) {
        mSelectQualifier = qualifierInt;
        return this;
    }

    /**
     * appends {@link #ALL} to the query
     *
     * @return
     */
    public Select all() {
        return selectQualifier(ALL);
    }

    /**
     * appends {@link #COUNT} to the query
     *
     * @return
     */
    public Select count() {
        return method("COUNT", "*");
    }

    /**
     * Appends a method to this query. Such methods as avg, count, sum.
     *
     * @param methodName
     * @param columnName
     * @return
     */
    public Select method(String methodName, String columnName) {
        mMethodName = methodName;
        mColumnName = columnName;
        return selectQualifier(METHOD);
    }

    /**
     * The Average of the column values
     *
     * @param columnName
     * @return
     */
    public Select avg(String columnName) {
        return method("AVG", columnName);
    }

    /**
     * Sums the specific column value
     *
     * @param columnName
     * @return
     */
    public Select sum(String columnName) {
        return method("SUM", columnName);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.append("SELECT").appendSpace();

        if (mSelectQualifier != NONE) {
            if (mSelectQualifier == DISTINCT) {
                queryBuilder.append("DISTINCT");
            } else if (mSelectQualifier == ALL) {
                queryBuilder.append("ALL");
            } else if (mSelectQualifier == METHOD) {
                queryBuilder.append(mMethodName.toUpperCase()).appendParenthesisEnclosed(QueryBuilder.quote(mColumnName));
            }
            queryBuilder.appendSpace();
        }

        if (mColumns != null && mColumns.length > 0) {
            String[] columns = new String[mColumns.length];
            for (int i=0; i < mColumns.length; i++)
                columns[i] = QueryBuilder.quote(mColumns[i]);
            queryBuilder.append(TextUtils.join(", ", columns));
        } else if (mSelectQualifier != METHOD) {
            queryBuilder.append("*");
        }
        queryBuilder.appendSpace();
        return queryBuilder.getQuery();
    }
}
