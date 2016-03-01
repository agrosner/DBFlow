package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: This class will use a String to describe its condition.
 * Not recommended for normal queries, but can be used as a fall-back.
 */
public class UnSafeStringCondition implements SQLCondition {

    private final String conditionString;
    private String separator = "";

    public UnSafeStringCondition(String selection, String[] selectionArgs) {
        String newSelection = selection;
        // replace question marks in order
        if (newSelection != null) {
            for (String selectionArg : selectionArgs) {
                newSelection = newSelection.replaceFirst("\\?", selectionArg);
            }
        }
        this.conditionString = newSelection;
    }

    @Override
    public void appendConditionToQuery(QueryBuilder queryBuilder) {
        queryBuilder.append(conditionString);
    }

    @Override
    public String columnName() {
        return "";
    }

    @Override
    public String separator() {
        return separator;
    }

    @Override
    public SQLCondition separator(String separator) {
        this.separator = separator;
        return this;
    }

    @Override
    public boolean hasSeparator() {
        return StringUtils.isNotNullOrEmpty(separator);
    }

    @Override
    public String operation() {
        return "";
    }

    @Override
    public Object value() {
        return "";
    }
}