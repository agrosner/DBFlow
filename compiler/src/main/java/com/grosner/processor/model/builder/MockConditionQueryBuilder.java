package com.grosner.processor.model.builder;

import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.processor.utils.ModelUtils;

import java.util.List;

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

    public MockConditionQueryBuilder appendEmptyMockConditions(List<String> fields) {
        boolean isFirst = true;
        for(String field: fields) {
            if(!isFirst) {
                append(",");
            } else {
                isFirst = false;
            }
            appendMockCondition(field, "\"?\"");
        }

        return this;
    }

    public MockConditionQueryBuilder appendMockCondition(String foreignColumnModelField, String modelStatement) {
        return append("Condition.column").appendParenthesisEnclosed(foreignColumnModelField).append(".is")
                .appendParenthesisEnclosed(modelStatement);
    }

    public MockConditionQueryBuilder appendMockContinueCondition(String foreignColumnModelField, String modelStatement) {
        return append(".putCondition").append("(").appendMockCondition(foreignColumnModelField, modelStatement).append(")");
    }

    public MockConditionQueryBuilder appendCreation(String modelClassName) {
        return appendEmptyCreation(modelClassName).append(", ");
    }

    public MockConditionQueryBuilder appendEmptyCreation(String modelClassName) {
        return append("new").appendSpace().append("ConditionQueryBuilder<").append(modelClassName)
                .append(">(").append(ModelUtils.getFieldClass(modelClassName));
    }

    public MockConditionQueryBuilder appendEndCreation() {
        return append(")");
    }
}
