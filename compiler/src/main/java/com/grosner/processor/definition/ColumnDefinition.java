package com.grosner.processor.definition;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.processor.Classes;
import com.grosner.processor.ProcessorUtils;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.model.builder.ForeignKeyReferenceBuilder;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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

    public TypeElement modelType;

    public TypeElement databaseType;

    public int columnType;

    public Element element;

    public Column column;

    public ForeignKeyReference[] foreignKeyReferences;

    public boolean isModel;

    public boolean isModelContainer;

    public ColumnDefinition(ProcessorManager processorManager, VariableElement element) {
        this.element = element;

        column = element.getAnnotation(Column.class);
        this.columnName = column.name().equals("") ? element.getSimpleName().toString() : column.name();
        this.columnFieldName = element.getSimpleName().toString();
        this.columnFieldType = element.asType().toString();
        this.modelType = processorManager.getElements().getTypeElement(element.asType().toString());
        columnType = column.columnType();

        if(columnType == Column.FOREIGN_KEY) {
            foreignKeyReferences = column.references();
        }

        isModel = ProcessorUtils.implementsClass(processorManager.getProcessingEnvironment(), Classes.MODEL, modelType);

        final TypeConverterDefinition typeConverterDefinition = processorManager.getTypeConverterDefinition(modelType);
        if(typeConverterDefinition != null) {
            databaseType = typeConverterDefinition.getDbElement();
        } else {
            databaseType = modelType;
        }

        isModelContainer = ProcessorUtils.implementsClass(processorManager.getProcessingEnvironment(), Classes.MODEL_CONTAINER, modelType);
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


    protected void writeColumnDefinition(JavaWriter javaWriter, String fieldName, String columnName) throws IOException {
        javaWriter.emitField("String", fieldName,
                Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL),
                "\""+columnName+"\"");
        javaWriter.emitEmptyLine();
    }

    public void writeContentValue(JavaWriter javaWriter) throws IOException {
        if(columnType == Column.FOREIGN_KEY && isModel) {
            javaWriter.emitEmptyLine();
            if(isModelContainer) {
                javaWriter.emitSingleLineComment("Saving Model Container To DB");
            } else {
                javaWriter.emitSingleLineComment("Saving Foreign Key References From Model");
            }
            javaWriter.emitStatement(ModelUtils.getModelStatement(columnFieldName) + ".save(false)");
            for(ForeignKeyReference foreignKeyReference: foreignKeyReferences) {
                String contentValueString = columnFieldName + ".";
                if(isModelContainer) {
                    // TODO: figure out how to append all correct values
                } else {
                    contentValueString += foreignKeyReference.foreignColumnName();
                }
                javaWriter.emitStatement(ModelUtils.getContentStatement(foreignKeyReference.columnName(), contentValueString));
            }
        } else {
            javaWriter.emitStatement(ModelUtils.getContentStatement(columnName, columnFieldName));
        }
    }

    public void writeCursorDefinition(JavaWriter javaWriter) throws IOException {
        if(columnType == Column.FOREIGN_KEY) {
            //TODO: This is wrong, should be using condition query builder

            javaWriter.emitEmptyLine();
            javaWriter.emitSingleLineComment("Loading Model Foreign Key");
            javaWriter.emitStatement(ModelUtils.getModelStatement(columnFieldName) + " = new Select().from(%1s).where()\n" +
                    "                        .%1s.querySingle()",
                    ModelUtils.getFieldClass(columnFieldType),
                    new ForeignKeyReferenceBuilder()
                    .appendForeignKeyReferences(columnFieldType + TableDefinition.DBFLOW_TABLE_TAG, columnName, foreignKeyReferences));
        } else {
            javaWriter.emitStatement(ModelUtils.getModelStatement(columnFieldName) + " = %1s", ModelUtils.getCursorStatement(columnFieldType, columnName));
        }
    }


}
