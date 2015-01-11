package com.raizlabs.android.dbflow.processor.model.builder;

import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.StatementMap;

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

    /**
     * Appends our variable name for containers or models
     * @param isMCDefinition If true, append modelContainer, otherwise append model
     * @return This instance
     */
    public AdapterQueryBuilder appendVariable(boolean isMCDefinition) {
        return append(isMCDefinition ? "modelContainer" : "model");
    }

    public AdapterQueryBuilder appendBindSQLiteStatement(int index, String columnFieldType) {
        return append("statement.bind").append(StatementMap.getStatement(SQLiteType.get(columnFieldType)))
                .append("(").append(index).append(",");
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
                .append(isLoading ? ".getModelValue(" : ".getDBValue(");
    }
}
