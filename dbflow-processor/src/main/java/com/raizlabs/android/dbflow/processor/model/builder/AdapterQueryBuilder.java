package com.raizlabs.android.dbflow.processor.model.builder;

import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: Used for writing our adapter classes by providing some reuse and helper methods.
 */
public class AdapterQueryBuilder extends QueryBuilder<AdapterQueryBuilder> {

    public AdapterQueryBuilder() {
    }

    public AdapterQueryBuilder(String string) {
        super(string);
    }

    public AdapterQueryBuilder appendQuotesEnclosed(String string) {
        return append("\"").append(string).append("\"");
    }

    public AdapterQueryBuilder appendPut(String key) {
        return append(".put(").appendQuotesEnclosed(key).append(",");
    }

    public AdapterQueryBuilder appendGetValue(String value) {
        return append("getValue(").appendQuotesEnclosed(value).append(")");
    }

    public AdapterQueryBuilder appendCast(String type) {
        return append("(").appendParenthesisEnclosed(type);
    }

}
