package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

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
    private final ColumnAlias[] columns;

    /**
     * Specifies column names to select from that don't get quoted.
     */
    private String[] rawColumns;

    /**
     * The method name we wish to execute
     */
    private String mMethodName;

    /**
     * The column name passed into the method name
     */
    private String mColumnName;

    /**
     * The operator used when this is a subquery.
     */
    private String outerQueryOperator;

    private Where baseWhere;

    /**
     * Empty constructor since other constructors have different vargs.
     */
    public Select() {
        columns = null;
    }

    /**
     * Creates this instance with the specified columns from the specified {@link com.raizlabs.android.dbflow.config.FlowManager}
     *
     * @param columns Quoted column names.
     */
    public Select(String... columns) {
        this.columns = new ColumnAlias[columns == null ? 0 : columns.length];
        if (columns != null) {
            for (int i = 0; i < this.columns.length; i++) {
                this.columns[i] = ColumnAlias.column(columns[i]);
            }
        }
    }

    /**
     * Creates this instance with the specified column aliases
     *
     * @param columns A set of column aliases that we use in our query.
     */
    public Select(ColumnAlias... columns) {
        this.columns = columns;
    }

    /**
     * Constructs a SELECT as part of a subquery.
     *
     * @param baseWhere The base WHERE clause where it comes from. This is appended before this query.
     * @param operator  The operator to use to append the outer query to this inner SELECT.
     */
    Select(Where baseWhere, String operator, ColumnAlias...columns) {
        this(columns);
        this.baseWhere = baseWhere;
        this.outerQueryOperator = operator;
    }

    /**
     * Constructs a SELECT as part of a subquery.
     *
     * @param baseWhere The base WHERE clause where it comes from. This is appended before this query.
     * @param operator  The operator to use to append the outer query to this inner SELECT.
     */
    Select(Where baseWhere, String operator, String...columns) {
        this(columns);
        this.baseWhere = baseWhere;
        this.outerQueryOperator = operator;
    }

    /**
     * Attach columns you don't want to quote or have custom string.
     *
     * @param rawColumns The column names that are unquoted.
     * @return This instance.
     */
    public Select rawColumns(String... rawColumns) {
        this.rawColumns = rawColumns;
        return this;
    }

    /**
     * Passes this statement to the {@link From}
     *
     * @param table        The model table to run this query on
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return the From part of this query
     */
    public <ModelClass extends Model> From<ModelClass> from(Class<ModelClass> table) {
        return new From<>(this, table);
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
     * @param qualifierInt Can be {@link #ALL}, {@link #NONE}, {@link #DISTINCT}, or {@link #METHOD}
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

    boolean isASubQuery() {
        return baseWhere != null;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();

        // sub query needs parenthesis
        if (isASubQuery()) {
            queryBuilder.append(baseWhere.getQuery());
            if (outerQueryOperator != null) {
                queryBuilder.appendSpaceSeparated(outerQueryOperator);
            }
            queryBuilder.append("(");
        }
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

        boolean hasColumns = (columns != null && columns.length > 0);
        boolean hasRawColumns = (rawColumns != null && rawColumns.length > 0);
        if (hasColumns || hasRawColumns) {
            if (hasColumns) {
                queryBuilder.appendQuotedArray(columns);
            }
            if (hasRawColumns) {
                if (hasColumns) {
                    queryBuilder.append(", ");
                }
                queryBuilder.appendArray(rawColumns);
            }
        } else if (mSelectQualifier != METHOD) {
            queryBuilder.append("*");
        }
        queryBuilder.appendSpace();
        return queryBuilder.getQuery();
    }
}
