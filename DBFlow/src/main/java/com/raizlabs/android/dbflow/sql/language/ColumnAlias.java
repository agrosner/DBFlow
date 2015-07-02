package com.raizlabs.android.dbflow.sql.language;

import android.text.TextUtils;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;

/**
 * Description: Represents a column name as an alias to its original name. EX: SELECT `money` AS `myMoney`. However
 * if a an asName is not specified, then at its base this simply represents a column.
 */
public class ColumnAlias implements Query {

    /**
     * @param columnName The name of the column that might have an alias in a query.
     * @return A new instance.
     */
    public static ColumnAlias column(String columnName) {
        return new ColumnAlias(columnName);
    }

    /**
     * Returns a `tableName`.`columnName` handy for {@link Condition}
     *
     * @param tableName  The name of table to reference.
     * @param columnName The column name to reference.
     * @return A new instance.
     */
    public static ColumnAlias columnWithTable(String tableName, String columnName) {
        return columnRaw("`" + tableName + "`.`" + columnName + "`");
    }

    /**
     * @param functionName The name of the function to call as the {@link Condition#column(ColumnAlias)}
     * @param columnNames  The name of columns to use as parameters to the specified function.
     * @return Creates a new instance with function name that quotes the specified columns.
     * EX: date(`myColumn`) -&gt; ColumnAlias.columnsWithFunction("date", "myColumn")
     */
    public static ColumnAlias columnsWithFunction(String functionName, String... columnNames) {
        return columnRaw(functionName + "(" + QueryBuilder.join("`, `", columnNames) + ")");
    }

    /**
     * @param functionName  The name of the function to call as the {@link Condition#column(ColumnAlias)}
     * @param columnAliases The alias' of columns to use as parameters to the specified function.
     * @return Creates a new instance with function name that quotes the specified columns.
     * EX: date(`myColumn`) -&gt; ColumnAlias.columnsWithFunction("date", ColumnAlias.column("myColumn"))
     */
    public static ColumnAlias columnsWithFunction(String functionName, ColumnAlias... columnAliases) {
        return columnRaw(functionName + "(" + QueryBuilder.join("`, `", columnAliases) + ")");
    }

    /**
     * @param columnName The name of the column that we use that we don't append as quoted.
     * @return A new instance.
     */
    public static ColumnAlias columnRaw(String columnName) {
        return new ColumnAlias(columnName).shouldQuoteColumnName(false);
    }

    private final String columnName;
    private boolean shouldQuote = true;
    private String asName;

    private ColumnAlias(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Appends the asName after the AS of the query.
     *
     * @param asName The name of the AS `someName`
     * @return This instance.
     */
    public ColumnAlias as(String asName) {
        this.asName = asName;
        return this;
    }

    /**
     * @param shouldQuote if false, ticks won't be appended to column names.
     * @return This instance.
     */
    public ColumnAlias shouldQuoteColumnName(boolean shouldQuote) {
        this.shouldQuote = shouldQuote;
        return this;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();
        if (shouldQuote) {
            queryBuilder.appendQuoted(columnName);
        } else {
            queryBuilder.append(columnName);
        }
        if (!TextUtils.isEmpty(asName)) {
            queryBuilder.appendSpaceSeparated("AS")
                    .appendQuoted(asName);
        }
        return queryBuilder.getQuery();
    }

    /**
     * @return The "AS" name from this column (quoted). If not specified, it will default to the original column name.
     */
    public String getAliasName() {
        return QueryBuilder.quote(!TextUtils.isEmpty(asName) ? asName : columnName);
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
