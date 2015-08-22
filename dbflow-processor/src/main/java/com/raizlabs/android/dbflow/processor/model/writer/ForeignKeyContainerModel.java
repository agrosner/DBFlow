package com.raizlabs.android.dbflow.processor.model.writer;

import com.raizlabs.android.dbflow.processor.model.builder.AdapterQueryBuilder;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

/**
 * Description:
 */
public class ForeignKeyContainerModel extends ContentValueModel {

    private String modelContainerName;

    private boolean isModelContainerDefinition = false;

    public ForeignKeyContainerModel(ColumnAccessModel accessModel, boolean isContentValues) {
        super(accessModel, isContentValues);
    }

    public void setModelContainerName(String modelContainerName) {
        this.modelContainerName = modelContainerName;
    }

    public void setIsModelContainerDefinition(boolean isModelContainerDefinition) {
        this.isModelContainerDefinition = isModelContainerDefinition;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        if(!isModelContainerDefinition) {
            super.write(javaWriter);
        } else {
            AdapterQueryBuilder adapterQueryBuilder = new AdapterQueryBuilder();

            AdapterQueryBuilder ifBuilder = new AdapterQueryBuilder()
                    .append(modelContainerName).append(".").appendGetValue(accessModel.getReferencedColumnFieldName());
            javaWriter.beginControlFlow("if (%1s != null) ", ifBuilder.getQuery());
            if (!isContentValues()) {
                adapterQueryBuilder.appendBindSQLiteStatement(getIndex(), accessModel.castedClass);
            } else {
                adapterQueryBuilder.appendContentValues().appendPut(accessModel.foreignKeyLocalColumnName);
            }
            adapterQueryBuilder
                    .appendCast(accessModel.castedClass)
                    .append(modelContainerName)
                    .append(".")
                    .appendGetValue(accessModel.referencedColumnFieldName)
                    .append("))");
            javaWriter.emitStatement(adapterQueryBuilder.getQuery());

            javaWriter.nextControlFlow("else");
            AdapterQueryBuilder elseNull = new AdapterQueryBuilder();
            if (isContentValues()) {
                elseNull.appendContentValues();
                elseNull.append(".putNull").appendParenthesisEnclosed(
                        "\"" + accessModel.foreignKeyLocalColumnName + "\"");
            } else {
                elseNull.append("statement.");
                elseNull.append("bindNull").appendParenthesisEnclosed(getIndex());
            }
            javaWriter.emitStatement(elseNull.getQuery());
            javaWriter.endControlFlow();
        }
    }

    public AdapterQueryBuilder getNullStatement() {
        AdapterQueryBuilder nullStatement = new AdapterQueryBuilder();
        if (isContentValues()) {
            nullStatement.appendContentValues();
            nullStatement.append(".putNull").appendParenthesisEnclosed(
                    "\"" + accessModel.foreignKeyLocalColumnName + "\"");
        } else {
            nullStatement.append("statement.");
            nullStatement.append("bindNull").appendParenthesisEnclosed(getIndex());
        }
        return nullStatement;
    }
}
