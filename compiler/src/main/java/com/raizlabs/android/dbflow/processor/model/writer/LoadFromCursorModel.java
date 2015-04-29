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

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        if(isModelContainerAdapter) {
            AdapterQueryBuilder adapterQueryBuilder = new AdapterQueryBuilder();
            adapterQueryBuilder.append(modelContainerName)
                    .appendPut(accessModel.referencedColumnFieldName)
                    .append(ModelUtils.getCursorStatement(
                            accessModel.castedClass, accessModel.foreignKeyLocalColumnName))
                    .append(")");
            javaWriter.emitStatement(adapterQueryBuilder.getQuery());
        } else {
            String cursorStatment = ModelUtils.getCursorStatement(accessModel.castedClass,
                                                                  accessModel.foreignKeyLocalColumnName);
            emitColumnAssignment(javaWriter, cursorStatment);
        }
    }

    private void emitColumnAssignment(JavaWriter javaWriter,
                                      String valueStatement) throws IOException {
        boolean isContainerFieldDefinition = accessModel.isModelContainerAdapter;
        boolean isWritingForContainers = accessModel.fieldIsAModelContainer;
        AdapterQueryBuilder queryBuilder = new AdapterQueryBuilder().appendVariable(isContainerFieldDefinition);
        if (isWritingForContainers) {
            queryBuilder.append(".").append(accessModel.columnName);
        }
        if (isContainerFieldDefinition) {
            queryBuilder.appendPut(accessModel.containerKeyName);
        } else if (isWritingForContainers) {
            queryBuilder.appendPut(accessModel.referencedColumnFieldName);
        } else {
            queryBuilder.append(".").append(accessModel.referencedColumnFieldName).appendSpaceSeparated("=");
        }
        if (accessModel.requiresTypeConverter && !isContainerFieldDefinition) {
            queryBuilder.appendTypeConverter(accessModel.castedClass, accessModel.castedClass, true);
        } else if (accessModel.isABlob) {
            queryBuilder.append(String.format("new %1s(", Blob.class.getName()));
        }

        queryBuilder.append(valueStatement);

        if (accessModel.requiresTypeConverter && !isContainerFieldDefinition) {
            queryBuilder.append("))");
        } else if (isContainerFieldDefinition || isWritingForContainers || accessModel.isABlob) {
            queryBuilder.append(")");
        }

        javaWriter.emitStatement(queryBuilder.toString());
    }
}
