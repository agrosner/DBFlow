package com.raizlabs.android.dbflow.processor.model.writer;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.model.builder.AdapterQueryBuilder;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.sql.Query;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Description: Stores information from a {@link Column} or per {@link ForeignKeyReference} to help handle how to write it.
 */
public class ColumnAccessModel implements Query {

    String localColumnName;

    String foreignKeyLocalColumnName;

    String foreignColumnName;

    String containerKeyName;

    boolean isContainerFieldDefinition;

    boolean isWritingForContainers;

    boolean isForeignKeyField = false;

    boolean isABlob;

    boolean requiresTypeConverter;

    String castedClass;

    boolean isPrimitive;

    public ColumnAccessModel(ProcessorManager manager, ColumnDefinition columnDefinition,
                             boolean isWritingForContainers) {
        this.isWritingForContainers = columnDefinition.isModelContainer;
        localColumnName = columnDefinition.columnName;
        foreignColumnName = columnDefinition.columnFieldName;
        containerKeyName = columnDefinition.containerKeyName;
        this.isContainerFieldDefinition = isWritingForContainers;
        requiresTypeConverter = columnDefinition.hasTypeConverter;

        // Normal field
        String newFieldType = null;

        // convert field type for what type converter reports
        if (requiresTypeConverter) {
            TypeConverterDefinition typeConverterDefinition = manager.getTypeConverterDefinition(
                    columnDefinition.modelType);
            if (typeConverterDefinition == null) {
                manager.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                                   String.format("No Type Converter found for %1s",
                                                                 columnDefinition.modelType));
            } else {
                newFieldType = typeConverterDefinition.getDbElement().asType().toString();
            }
        } else {
            newFieldType = columnDefinition.columnFieldType;
        }

        if (isWritingForContainers) {
            if (columnDefinition.element.asType().getKind().isPrimitive() && !requiresTypeConverter) {
                newFieldType = manager.getTypeUtils().boxedClass(
                        (PrimitiveType) columnDefinition.element.asType()).asType().toString();
            }
        }
        castedClass = newFieldType;
        isABlob = columnDefinition.isBlob;
        isPrimitive = columnDefinition.element.asType().getKind().isPrimitive();
    }

    public ColumnAccessModel(ColumnDefinition columnDefinition, ForeignKeyReference foreignKeyReference,
                             boolean isWritingForContainers) {
        this.isWritingForContainers = isWritingForContainers;
        localColumnName = columnDefinition.columnName;
        foreignKeyLocalColumnName = foreignKeyReference.columnName();
        foreignColumnName = foreignKeyReference.foreignColumnName();
        containerKeyName = foreignKeyReference.foreignColumnName();
        isForeignKeyField = true;
        requiresTypeConverter = false;
        TypeMirror castClass = ModelUtils.getTypeMirrorFromAnnotation(foreignKeyReference);
        castedClass = castClass.toString();
        isABlob = false;
        isPrimitive = castClass.getKind().isPrimitive();
    }

    @Override
    public String getQuery() {
        AdapterQueryBuilder contentValue = new AdapterQueryBuilder();

        if (!requiresTypeConverter) {
            if (castedClass != null) {
                if (!isABlob) {
                    contentValue.appendCast(castedClass);
                } else {
                    contentValue.appendCast("byte[]");
                }
            } else {
                contentValue.append("(");
            }
        }
        contentValue.appendVariable(isContainerFieldDefinition).append(".");
        if (isContainerFieldDefinition) {
            contentValue.appendGetValue(containerKeyName);
        } else if (isWritingForContainers) {
            contentValue.append(localColumnName)
                    .append(".")
                    .appendGetValue(foreignColumnName);
        } else {
            if (isForeignKeyField) {
                contentValue.append(localColumnName)
                        .append(".");
            }
            contentValue.append(foreignColumnName);
        }

        if (isABlob) {
            contentValue.append(".getBlob()");
        }

        if (!requiresTypeConverter) {
            contentValue.append(")");
        }
        return contentValue.getQuery();
    }
}
