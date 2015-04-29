package com.raizlabs.android.dbflow.processor.model.writer;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.model.builder.AdapterQueryBuilder;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.sql.Query;

import javax.lang.model.element.Element;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Description: Stores information from a {@link Column} or per {@link ForeignKeyReference} to help handle how to write it.
 */
public class ColumnAccessModel implements Query {

    String columnName;

    String foreignKeyLocalColumnName;

    String referencedColumnFieldName;

    /**
     * Always the name of the field we're referencing.
     */
    String columnFieldName;

    String containerKeyName;

    /**
     * Always the type of the {@link Element} from {@link ColumnDefinition}
     */
    String columnFieldActualType;

    /**
     * Completely erased type used in database ops
     */
    String columnFieldType;

    boolean isModelContainerAdapter;

    boolean fieldIsAModelContainer;

    boolean isForeignKeyField = false;

    boolean isABlob;

    boolean requiresTypeConverter;

    String castedClass;

    boolean isPrimitive;

    public ColumnAccessModel(ProcessorManager manager, ColumnDefinition columnDefinition,
                             boolean isModelContainerAdapter) {
        this.fieldIsAModelContainer = columnDefinition.fieldIsModelContainer;
        columnName = columnDefinition.columnName;
        columnFieldName = columnDefinition.columnFieldName;
        columnFieldType = columnDefinition.columnFieldType;
        columnFieldActualType = columnDefinition.columnFieldActualType;
        referencedColumnFieldName = columnDefinition.columnFieldName;
        containerKeyName = columnDefinition.containerKeyName;
        this.isModelContainerAdapter = isModelContainerAdapter;
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

        if (isModelContainerAdapter) {
            if (columnDefinition.element.asType().getKind().isPrimitive() && !requiresTypeConverter) {
                newFieldType = manager.getTypeUtils().boxedClass(
                        (PrimitiveType) columnDefinition.element.asType()).asType().toString();
            }
        }
        castedClass = newFieldType;
        isABlob = columnDefinition.isBlob;
        isPrimitive = columnDefinition.element.asType().getKind().isPrimitive();
    }

    public ColumnAccessModel(ColumnDefinition columnDefinition, ForeignKeyReference foreignKeyReference) {
        this.fieldIsAModelContainer = columnDefinition.fieldIsModelContainer;
        columnName = columnDefinition.columnName;
        columnFieldActualType = columnDefinition.columnFieldActualType;
        columnFieldName = columnDefinition.columnFieldName;
        columnFieldType = columnDefinition.columnFieldType;
        foreignKeyLocalColumnName = foreignKeyReference.columnName();
        referencedColumnFieldName = foreignKeyReference.foreignColumnName();
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
                contentValue.appendCast(isABlob ? "byte[]" : castedClass);
            } else {
                contentValue.append("(");
            }
        }
        contentValue.appendVariable(isModelContainerAdapter).append(".");
        if (isModelContainerAdapter) {
            contentValue.appendGetValue(containerKeyName);
        } else if (fieldIsAModelContainer) {
            contentValue.append(columnName)
                    .append(".")
                    .appendGetValue(referencedColumnFieldName);
        } else {
            if (isForeignKeyField) {
                contentValue.append(columnName)
                        .append(".");
            }
            contentValue.append(referencedColumnFieldName);
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
