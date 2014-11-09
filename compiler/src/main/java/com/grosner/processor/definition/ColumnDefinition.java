package com.grosner.processor.definition;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.dbflow.sql.SQLiteType;
import com.grosner.processor.Classes;
import com.grosner.processor.ProcessorUtils;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.model.builder.AdapterQueryBuilder;
import com.grosner.processor.model.builder.MockConditionQueryBuilder;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.tools.Diagnostic;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ColumnDefinition implements FlowWriter {

    public String columnName;

    public String columnFieldName;

    public String columnFieldType;

    private String modelContainerType;

    public TypeElement modelType;

    public boolean hasTypeConverter = false;

    public int columnType;

    public Element element;

    public Column column;

    public ForeignKeyReference[] foreignKeyReferences;

    public boolean isModel;

    private ProcessorManager processorManager;

    /**
     * Whether this field is itself a model container
     */
    public boolean isModelContainer;

    public ColumnDefinition(ProcessorManager processorManager, VariableElement element) {
        this.processorManager = processorManager;
        this.element = element;

        column = element.getAnnotation(Column.class);
        this.columnName = column.name().equals("") ? element.getSimpleName().toString() : column.name();
        this.columnFieldName = element.getSimpleName().toString();
        this.columnFieldType = element.asType().toString();

        if (element.asType().getKind().isPrimitive()) {
            this.modelType = processorManager.getTypeUtils().boxedClass((PrimitiveType) element.asType());
        } else {
            boolean isAModelContainer = false;
            DeclaredType declaredType = null;
            if(element.asType() instanceof DeclaredType) {
                declaredType = (DeclaredType) element.asType();
                isAModelContainer = !declaredType.getTypeArguments().isEmpty();
            } else if(element.asType() instanceof ArrayType) {
                processorManager.getMessager().printMessage(Diagnostic.Kind.ERROR, "Columns cannot be of array type.");
            }
            if (isAModelContainer) {
                isModelContainer = true;
                // TODO: hack for now
                modelContainerType = columnFieldType;
                this.modelType = (TypeElement) processorManager.getTypeUtils().asElement(declaredType.getTypeArguments().get(0));
                columnFieldType = modelType.asType().toString();
            } else {
                this.modelType = processorManager.getElements().getTypeElement(element.asType().toString());
            }
        }

        columnType = column.columnType();

        if(columnType == Column.FOREIGN_KEY) {
            foreignKeyReferences = column.references();
        }

        isModel = ProcessorUtils.implementsClass(processorManager.getProcessingEnvironment(), Classes.MODEL, modelType);

        // Any annotated members, otherwise we will use the scanner to find other ones
        final TypeConverterDefinition typeConverterDefinition = processorManager.getTypeConverterDefinition(modelType);
        if (typeConverterDefinition != null) {
            hasTypeConverter = true;
        }

        // If type cannot be represented, we will get Type converter anyways
        if(!hasTypeConverter && !isModel) {
            hasTypeConverter = !SQLiteType.containsClass(columnFieldType);
        }

        //isModelContainer = ProcessorUtils.implementsClass(processorManager.getProcessingEnvironment(), Classes.MODEL_CONTAINER, modelType);
    }


    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        if(isModel || isModelContainer) {
            for(ForeignKeyReference reference: foreignKeyReferences) {
                writeColumnDefinition(javaWriter, (columnName + "_" + reference.columnName()).toUpperCase(), reference.columnName());
            }
        } else {
            writeColumnDefinition(javaWriter, columnName);
        }
    }

    protected void writeColumnDefinition(JavaWriter javaWriter, String columnName)  throws IOException {
        writeColumnDefinition(javaWriter, columnName.toUpperCase(), columnName);
    }

    /**
     * When the field name is different from the column name (foreign key names)
     * @param javaWriter
     * @param fieldName
     * @param columnName
     * @throws IOException
     */
    protected void writeColumnDefinition(JavaWriter javaWriter, String fieldName, String columnName) throws IOException {
        javaWriter.emitField("String", fieldName,
                Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL),
                "\""+columnName+"\"");
        javaWriter.emitEmptyLine();
    }

    public void writeSaveDefinition(JavaWriter javaWriter, boolean isContainer) throws IOException {
        if(columnType == Column.FOREIGN_KEY && isModel) {
            javaWriter.emitEmptyLine();
            if(isModelContainer) {
                javaWriter.emitSingleLineComment("Begin Saving Model Container To DB");
            } else {
                javaWriter.emitSingleLineComment("Begin Saving Foreign Key References From Model");
            }
            javaWriter.emitStatement((isContainer ? ModelUtils.getCastedValue(columnFieldType, ModelUtils.getContainerStatement(columnFieldName)) :
                    ModelUtils.getModelStatement(columnFieldName)) + ".save(false)");
            for(ForeignKeyReference foreignKeyReference: foreignKeyReferences) {
                javaWriter.emitStatement(ModelUtils.getContentValueStatement(foreignKeyReference.columnName(),
                        columnName, ModelUtils.getClassFromAnnotation(foreignKeyReference),
                        foreignKeyReference.foreignColumnName(),
                        isContainer, isModelContainer, true, false, columnFieldType));
            }
            javaWriter.emitSingleLineComment("End");
            javaWriter.emitEmptyLine();
        } else {
            String newFieldType;
            if(hasTypeConverter) {
                TypeConverterDefinition typeConverterDefinition = processorManager.getTypeConverterDefinition(modelType);
                if(typeConverterDefinition == null) {
                    processorManager.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("No Type Converter found for %1s", modelType));
                }
                newFieldType = typeConverterDefinition.getDbElement().asType().toString();
            } else {
                newFieldType = columnFieldType;
            }
            javaWriter.emitStatement(ModelUtils.getContentValueStatement(columnName, columnName,
                    newFieldType, columnFieldName, isContainer, isModelContainer, false, hasTypeConverter, columnFieldType));
        }
    }

    public void writeLoadFromCursorDefinition(JavaWriter javaWriter, boolean isModelContainerDefinition) throws IOException {
        if(columnType == Column.FOREIGN_KEY) {
            //TODO: This is wrong, should be using condition query builder
            javaWriter.emitEmptyLine();
            javaWriter.emitSingleLineComment("Begin Loading %1s Model Foreign Key", columnFieldName);

            // special case for model objects within class
            if(!isModelContainer && !isModelContainerDefinition && isModel) {
                MockConditionQueryBuilder conditionQueryBuilder = new MockConditionQueryBuilder()
                        .appendForeignKeyReferences(columnFieldType + TableDefinition.DBFLOW_TABLE_TAG, columnName, foreignKeyReferences);

                String rawConditionStatement = String.format("new Select().from(%1s).where().%1s.querySingle()",
                        ModelUtils.getFieldClass(columnFieldType), conditionQueryBuilder);

                AdapterQueryBuilder adapterQueryBuilder = new AdapterQueryBuilder().appendVariable(false);
                adapterQueryBuilder.append(".").append(columnFieldName).appendSpaceSeparated("=");
                adapterQueryBuilder.append(rawConditionStatement);
                javaWriter.emitStatement(adapterQueryBuilder.getQuery());
            } else {
                for(ForeignKeyReference foreignKeyReference: foreignKeyReferences) {
                    javaWriter.emitStatement(ModelUtils.getLoadFromCursorDefinitionField(processorManager, ModelUtils.getClassFromAnnotation(foreignKeyReference),
                            columnFieldName, foreignKeyReference.columnName(), foreignKeyReference.foreignColumnName(), null, false, isModelContainerDefinition, isModelContainer));
                }
            }
            javaWriter.emitSingleLineComment("End");
            javaWriter.emitEmptyLine();

        } else {
            javaWriter.emitStatement(ModelUtils.getLoadFromCursorDefinitionField(processorManager, columnFieldType, columnFieldName,
                    columnName, "", modelType, hasTypeConverter, isModelContainerDefinition, this.isModelContainer));
        }
    }

    public void writeToModelDefinition(JavaWriter javaWriter) throws IOException {
        AdapterQueryBuilder queryBuilder = new AdapterQueryBuilder();
        queryBuilder.appendVariable(false).append(".").append(columnFieldName);
        queryBuilder.appendSpaceSeparated("=");

        if(hasTypeConverter) {
            queryBuilder.appendTypeConverter(columnFieldType, columnFieldType, true);
        } else {
            queryBuilder.appendCast(isModelContainer ? modelContainerType : columnFieldType);
        }
        queryBuilder.appendVariable(true).append(".").appendGetValue(columnFieldName).append(")");

        if(hasTypeConverter) {
            queryBuilder.append(")");
        }

        javaWriter.emitStatement(queryBuilder.getQuery());
    }


}
