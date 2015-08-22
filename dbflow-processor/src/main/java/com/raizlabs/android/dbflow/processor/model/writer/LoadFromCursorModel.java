package com.raizlabs.android.dbflow.processor.model.writer;

import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.processor.model.builder.AdapterQueryBuilder;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

/**
 * Description:
 */
public class LoadFromCursorModel implements FlowWriter {

    private final ColumnAccessModel accessModel;

    private boolean isNullable;

    private boolean isModelContainerAdapter;

    private String modelContainerName;

    public LoadFromCursorModel(ColumnAccessModel accessModel) {
        this.accessModel = accessModel;
    }

    public void setIsNullable(boolean isNullable) {
        this.isNullable = isNullable;
    }

    public void setIsModelContainerAdapter(boolean isModelContainerAdapter) {
        this.isModelContainerAdapter = isModelContainerAdapter;
    }

    public void setModelContainerName(String modelContainerName) {
        this.modelContainerName = modelContainerName;
    }

    public void writeSingleField(JavaWriter javaWriter) throws IOException {

        javaWriter.emitStatement(ModelUtils.getColumnIndex(accessModel.foreignKeyLocalColumnName));
        String index = "index" + accessModel.foreignKeyLocalColumnName;
        javaWriter.beginControlFlow("if (%1s != -1) ", index);
        if (isNullable) {
            javaWriter.beginControlFlow("if (cursor.isNull(%1s))", index);
            emitColumnAssignment(javaWriter, "null");
            javaWriter.nextControlFlow("else");
            write(javaWriter);
            javaWriter.endControlFlow();
        } else {
            write(javaWriter);
        }
        javaWriter.endControlFlow();
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        String cursorStatementClass = accessModel.castedClass;
        if (accessModel.isEnum) {
            cursorStatementClass = String.class.getName();
        }
        String cursorStatment = ModelUtils.getCursorStatement(cursorStatementClass,
                accessModel.foreignKeyLocalColumnName);
        emitColumnAssignment(javaWriter, cursorStatment);
    }

    private void emitColumnAssignment(JavaWriter javaWriter,
                                      String valueStatement) throws IOException {
        boolean isContainerFieldDefinition = accessModel.isModelContainerAdapter;
        boolean fieldIsAModelContainer = accessModel.fieldIsAModelContainer;
        boolean isNull = valueStatement.equals("null");
        AdapterQueryBuilder queryBuilder = new AdapterQueryBuilder();

        if (isModelContainerAdapter) {
            if (accessModel.isForeignKeyField) {
                queryBuilder.append(modelContainerName);
            } else {
                queryBuilder.append(ModelUtils.getVariable(true));
            }
        } else {
            queryBuilder.appendVariable(isContainerFieldDefinition);
        }
        if (fieldIsAModelContainer && isModelContainerAdapter) {
            queryBuilder.appendPut(accessModel.getReferencedColumnFieldName());
        } else if (fieldIsAModelContainer) {
            queryBuilder.append(".").append(accessModel.columnFieldName)
                    .appendPut(accessModel.getReferencedColumnFieldName());
        } else if (isModelContainerAdapter && accessModel.isForeignKeyField) {
            queryBuilder.appendPut(accessModel.getReferencedColumnFieldName());
        }
        if (isContainerFieldDefinition) {
            queryBuilder.appendPut(accessModel.containerKeyName);
        } else if (!fieldIsAModelContainer && !isModelContainerAdapter) {
            queryBuilder.append(".")
                    .append(accessModel.getSetterReferenceColumnFieldName());
            if (!accessModel.isPrivate) {
                queryBuilder.appendSpaceSeparated("=");
            }
        }
        if (accessModel.isEnum) {
            // don't attempt to use valueOf on a null, will throw a NullPointerException
            if (!isNull) {
                queryBuilder.append(accessModel.castedClass)
                        .append(".valueOf(");
            }
        } else {
            if (accessModel.requiresTypeConverter && !isNull) {
                queryBuilder.appendTypeConverter(accessModel.columnFieldBoxedType, accessModel.columnFieldBoxedType,
                        true);
            } else if (accessModel.isABlob) {
                queryBuilder.append(String.format("new %1s(", Blob.class.getName()));
            }
        }

        queryBuilder.append(valueStatement);

        if (accessModel.requiresTypeConverter && !isNull && !accessModel.isEnum ||
                (accessModel.isEnum && isContainerFieldDefinition && !isNull)) {
            queryBuilder.append("))");
        } else if (isModelContainerAdapter || isContainerFieldDefinition || fieldIsAModelContainer || accessModel.isABlob ||
                (accessModel.isEnum && !isNull)) {
            queryBuilder.append(")");
        }

        if (accessModel.isPrivate && !isContainerFieldDefinition) {
            queryBuilder.append(")");
        }

        if ((accessModel.requiresTypeConverter) && !accessModel.isEnum()
                && isModelContainerAdapter && !isNull  || isModelContainerAdapter && accessModel.isABlob) {
            queryBuilder.append(")");
        }

        javaWriter.emitStatement(queryBuilder.toString());
    }
}
