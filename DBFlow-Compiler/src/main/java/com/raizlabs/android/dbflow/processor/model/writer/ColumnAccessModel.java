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

    String columnFieldBoxedType;

    boolean isModelContainerAdapter;

    boolean fieldIsAModelContainer;

    boolean isForeignKeyField = false;

    boolean isABlob;

    boolean requiresTypeConverter;

    String castedClass;

    boolean isPrimitive;

    boolean isPrivate;

    boolean isEnum;

    String setterName;

    String getterName;

    ColumnDefinition parentColumnDefinition;

    public ColumnAccessModel(ProcessorManager manager, ColumnDefinition columnDefinition,
                             boolean isModelContainerAdapter) {
        parentColumnDefinition = columnDefinition;
        this.fieldIsAModelContainer = columnDefinition.fieldIsModelContainer;
        columnName = columnDefinition.columnName;
        columnFieldName = columnDefinition.columnFieldName;
        columnFieldType = columnDefinition.columnFieldType;
        columnFieldActualType = columnDefinition.columnFieldActualType;
        referencedColumnFieldName = columnDefinition.columnFieldName;
        foreignKeyLocalColumnName = columnName;
        containerKeyName = columnDefinition.containerKeyName;
        isPrivate = columnDefinition.isPrivate;
        setterName = columnDefinition.setterName;
        getterName = columnDefinition.getterName;

        this.isModelContainerAdapter = isModelContainerAdapter;
        requiresTypeConverter = columnDefinition.hasTypeConverter;
        isEnum = columnDefinition.isEnum;

        // Normal field
        String newFieldType = null;

        // convert field type for what type converter reports
        if (requiresTypeConverter && !isEnum) {
            TypeConverterDefinition typeConverterDefinition = manager.getTypeConverterDefinition(
                    columnDefinition.modelType);
            if (typeConverterDefinition == null) {
                manager.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR,
                                      String.format("No Type Converter found for %1s",
                                                    columnDefinition.modelType));
            } else {
                newFieldType = typeConverterDefinition.getDbElement()
                        .asType()
                        .toString();
            }
        } else {
            newFieldType = columnDefinition.columnFieldType;
        }

        if (isModelContainerAdapter) {
            if (columnDefinition.element.asType()
                        .getKind()
                        .isPrimitive() && !requiresTypeConverter) {
                newFieldType = manager.getTypeUtils()
                        .boxedClass(
                                (PrimitiveType) columnDefinition.element.asType())
                        .asType()
                        .toString();
            }
        }
        castedClass = newFieldType;
        isABlob = columnDefinition.isBlob;
        isPrimitive = columnDefinition.element.asType()
                .getKind()
                .isPrimitive();

        if (isPrimitive) {
            columnFieldBoxedType = manager.getTypeUtils()
                    .boxedClass((PrimitiveType) columnDefinition.element.asType())
                    .asType()
                    .toString();
        } else {
            columnFieldBoxedType = columnFieldActualType;
        }
    }

    public ColumnAccessModel(ColumnDefinition columnDefinition, ForeignKeyReference foreignKeyReference) {
        parentColumnDefinition = columnDefinition;
        this.fieldIsAModelContainer = columnDefinition.fieldIsModelContainer;
        columnName = columnDefinition.columnName;
        setterName = columnDefinition.setterName;
        getterName = columnDefinition.getterName;
        columnFieldActualType = columnDefinition.columnFieldActualType;
        columnFieldName = columnDefinition.columnFieldName;
        columnFieldType = columnDefinition.columnFieldType;
        isPrivate = foreignKeyReference.fieldIsPrivate();
        foreignKeyLocalColumnName = foreignKeyReference.columnName();
        referencedColumnFieldName = columnDefinition.isModel ? foreignKeyReference.foreignColumnName()
                : columnDefinition.columnFieldName;
        containerKeyName = foreignKeyReference.foreignColumnName();
        isForeignKeyField = true;
        requiresTypeConverter = false;
        TypeMirror castClass = ModelUtils.getTypeMirrorFromAnnotation(foreignKeyReference);
        castedClass = castClass.toString();
        isABlob = false;
        isEnum = false;
        isPrimitive = castClass.getKind()
                .isPrimitive();
        columnFieldBoxedType = columnFieldActualType;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public boolean isRequiresTypeConverter() {
        return requiresTypeConverter;
    }

    public String getQueryNoCast() {
        return getQuery(false);
    }

    @Override
    public String getQuery() {
        return getQuery(true);
    }

    public String getQuery(boolean useCastIfApplicable) {
        AdapterQueryBuilder contentValue = new AdapterQueryBuilder();

        if (!requiresTypeConverter) {
            if (castedClass != null && useCastIfApplicable) {
                contentValue.appendCast(isABlob ? "byte[]" : castedClass);
            } else {
                contentValue.append("(");
            }
        }
        contentValue.appendVariable(isModelContainerAdapter)
                .append(".");
        if (isModelContainerAdapter) {
            contentValue.appendGetValue(containerKeyName);
        } else if (fieldIsAModelContainer) {
            contentValue.append(columnName)
                    .append(".")
                    .appendGetValue(referencedColumnFieldName);
        } else {
            if (isForeignKeyField) {
                ColumnAccessModel columnAccessModel = new ColumnAccessModel(parentColumnDefinition.getManager(), parentColumnDefinition, isModelContainerAdapter);
                contentValue.append(columnAccessModel.getReferencedColumnFieldName())
                        .append(".");
            }
            contentValue.append(getReferencedColumnFieldName());
        }

        if (isABlob) {
            contentValue.append(".getBlob()");
        }

        if (!requiresTypeConverter) {
            contentValue.append(")");
        }
        return contentValue.getQuery();
    }

    private String getPrivateGetterMethodName() {
        if (getterName == null || getterName.length() == 0) {
            String newName =
                    referencedColumnFieldName.substring(0, 1)
                            .toUpperCase() +
                    (referencedColumnFieldName.length() > 1 ? referencedColumnFieldName.substring(1) : "");
            return "get" + newName + "()";
        } else {
            if(getterName.startsWith("get")) {
                return getterName + "()";
            } else {
                return "get" + getterName + "()";
            }
        }
    }

    private String getPrivateSetterMethodName() {
        if (setterName == null || setterName.length() == 0) {
            String newName =
                    referencedColumnFieldName.substring(0, 1)
                            .toUpperCase() +
                    (referencedColumnFieldName.length() > 1 ? referencedColumnFieldName.substring(1) : "");
            return "set" + newName + "(";
        } else {
            if(setterName.startsWith("set")) {
                return setterName + "(";
            } else {
                return "set" + setterName + "(";
            }
        }
    }

    public String getReferencedColumnFieldName() {
        return isPrivate ? getPrivateGetterMethodName() : referencedColumnFieldName;
    }

    public String getSetterReferenceColumnFieldName() {
        return isPrivate ? getPrivateSetterMethodName() : referencedColumnFieldName;
    }

    public String getCastedClass() {
        return castedClass;
    }
}
