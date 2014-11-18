package com.grosner.processor.model.builder;

import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.processor.utils.ModelUtils;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
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

    public AdapterQueryBuilder appendVariable(boolean isMCDefinition) {
        return append(isMCDefinition ? "modelContainer" : "model");
    }

    public AdapterQueryBuilder appendContentValues() {
        return append("contentValues");
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

    public AdapterQueryBuilder appendClass(String className) {
        return append(className).append(".class");
    }

    public AdapterQueryBuilder appendTypeConverter(String fieldReturnType, String modelClassName, boolean isLoading) {
        return appendCast(fieldReturnType).append("FlowManager.getTypeConverterForClass(")
                .append(ModelUtils.getFieldClass(modelClassName)).append(")")
                .append(isLoading ?  ".getModelValue(": ".getDBValue(");
    }
}
