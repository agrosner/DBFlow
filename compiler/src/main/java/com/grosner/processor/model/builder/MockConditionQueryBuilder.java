package com.grosner.processor.model.builder;

import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.processor.utils.ModelUtils;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class MockConditionQueryBuilder extends QueryBuilder<MockConditionQueryBuilder> {

    public MockConditionQueryBuilder() {
    }

    public MockConditionQueryBuilder(String string) {
        super(string);
    }

    public MockConditionQueryBuilder appendForeignKeyReferences(String foreignColumnTableClass, String columnName,
                                                                 ForeignKeyReference[] foreignKeyReferences) {
        for(int i = 0; i < foreignKeyReferences.length; i++) {
            ForeignKeyReference foreignKeyReference = foreignKeyReferences[i];
            append("and(").appendMockCursorCondition(ModelUtils.getStaticMember(foreignColumnTableClass, foreignKeyReference.foreignColumnName()),
                    ModelUtils.getClassFromAnnotation(foreignKeyReference),
                    foreignKeyReference.columnName()).append(")");

            if(i < foreignKeyReferences.length - 1) {
                append(".");
            }
        }

        return this;
    }

    public MockConditionQueryBuilder appendMockCursorCondition(String foreignColumnModelField, String fieldType, String localColumn) {
        return appendMockCondition(foreignColumnModelField, ModelUtils.getCursorStatement(fieldType, localColumn));
    }

    public MockConditionQueryBuilder appendMockCondition(String foreignColumnModelField, String modelStatement) {
        append("Condition.column").appendParenthesisEnclosed(foreignColumnModelField).append(".is")
                .appendParenthesisEnclosed(modelStatement);
        return this;
    }

    public MockConditionQueryBuilder appendCreation(String modelClassName) {
        return append("new").appendSpace().append("ConditionQueryBuilder<").append(modelClassName)
                .append(">(").append(ModelUtils.getFieldClass(modelClassName)).append(", ");
    }

    public MockConditionQueryBuilder appendEndCreation() {
        return append(")");
    }
}
