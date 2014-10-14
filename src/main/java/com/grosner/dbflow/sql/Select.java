package com.grosner.dbflow.sql;

import android.text.TextUtils;

import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: A SQL SELECT statement generator. It generates the SELECT part of the statement.
 */
public class Select implements Query {

    /**
     * Default does not include the qualifier
     */
    public static final int NONE = -1;

    /**
     * SELECT DISTINCT call
     */
    public static final int DISTINCT = 0;

    /**
     * SELECT ALL call
     */
    public static final int ALL = 1;

    /**
     * SELECT COUNT(*) method call
     */
    public static final int COUNT = 2;

    /**
     * Specifies the column names to select from
     */
    private final String[] mColumns;

    /**
     * The select qualifier to append to the SELECT statement
     */
    private int mSelectQualifier = NONE;

    /**
     * Creates this instance with the specified columns from the specified {@link com.grosner.dbflow.config.FlowManager}
     *
     * @param columns
     */
    public Select(String... columns) {
        mColumns = columns;
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
     * appends {@link #DISTINCT} to the query
     *
     * @return
     */
    public Select distinct() {
        return selectQualifier(DISTINCT);
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
        return selectQualifier(COUNT);
    }

    /**
     * Passes this statement to the {@link com.grosner.dbflow.sql.From}
     *
     * @param table        The model table to run this query on
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return the From part of this query
     */
    public <ModelClass extends Model> From<ModelClass> from(Class<ModelClass> table) {
        return new From<ModelClass>(this, table);
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
            } else if (mSelectQualifier == COUNT) {
                queryBuilder.append("COUNT(*)");
            }
            queryBuilder.appendSpace();
        }

        if (mColumns != null && mColumns.length > 0) {
            queryBuilder.append(TextUtils.join(", ", mColumns));
        } else if (mSelectQualifier != COUNT) {
            queryBuilder.append("*");
        }
        queryBuilder.appendSpace();
        return queryBuilder.getQuery();
    }
}
