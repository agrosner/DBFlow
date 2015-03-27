package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
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
     * appends COUNT to the query
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
                queryBuilder.append(mMethodName.toUpperCase()).append("(").appendQuoted(mColumnName).append(")");
            }
            queryBuilder.appendSpace();
        }

        if (mColumns != null && mColumns.length > 0) {
            queryBuilder.appendQuotedArray(mColumns);
        } else if (mSelectQualifier != METHOD) {
            queryBuilder.append("*");
        }
        queryBuilder.appendSpace();
        return queryBuilder.getQuery();
    }
}
