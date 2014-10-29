package com.grosner.processor.model.builder;

import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.processor.utils.ModelUtils;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ForeignKeyReferenceBuilder extends QueryBuilder<ForeignKeyReferenceBuilder> {

    public ForeignKeyReferenceBuilder appendForeignKeyReferences(String foreignColumnTableClass, String columnName,
                                                                 ForeignKeyReference[] foreignKeyReferences) {
        for(int i = 0; i < foreignKeyReferences.length; i++) {
            ForeignKeyReference foreignKeyReference = foreignKeyReferences[i];
            append("and(").appendMockCondition(ModelUtils.getStaticMember(foreignColumnTableClass, foreignKeyReference.foreignColumnName()),
                    foreignKeyReference.columnName(),
                    "java.lang.String").append(")");

            if(i < foreignKeyReferences.length - 1) {
                append(".");
            }
        }

        return this;
    }

    public ForeignKeyReferenceBuilder appendMockCondition(String foreignColumnModelField, String localColumn, String fieldType) {
        append("Condition.column").appendParenthesisEnclosed(foreignColumnModelField).append(".is")
                .appendParenthesisEnclosed(ModelUtils.getCursorStatement(fieldType, localColumn));
        return this;
    }
}
