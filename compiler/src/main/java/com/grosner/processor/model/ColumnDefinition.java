package com.grosner.processor.model;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.processor.ProcessorUtils;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ColumnDefinition implements FlowWriter{

    String columnName;

    String columnFieldName;

    String columnFieldType;

    int columnType;

    Element element;

    ForeignKeyReference[] foreignKeyReferences;

    boolean isModel;

    boolean isModelContainer;

    public ColumnDefinition(ProcessingEnvironment processingEnvironment, VariableElement element) {
        this.element = element;

        Column column = element.getAnnotation(Column.class);
        this.columnName = column.name().equals("") ? element.getSimpleName().toString() : column.name();
        this.columnFieldName = element.getSimpleName().toString();
        this.columnFieldType = element.asType().toString();
        columnType = column.columnType();

        if(columnType == Column.FOREIGN_KEY) {
            foreignKeyReferences = column.references();
        }

        isModel = ProcessorUtils.implementsClass(processingEnvironment, "com.grosner.dbflow.structure.Model", element);

        isModelContainer = ProcessorUtils.implementsClass(processingEnvironment, "com.grosner.dbflow.structure.container.ModelContainer",
                element);
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
            javaWriter.emitStatement(getModelStatement(columnFieldName) + ".save(false)");
            for(ForeignKeyReference foreignKeyReference: foreignKeyReferences) {
                String contentValueString = columnFieldName + ".";
                if(isModelContainer) {
                    // TODO: figure out how to append all correct values
                } else {
                    contentValueString += foreignKeyReference.foreignColumnName();
                }
                javaWriter.emitStatement(getContentStatement(foreignKeyReference.columnName(), contentValueString));
            }
        } else {
            javaWriter.emitStatement(getContentStatement(columnName, columnFieldName));
        }
    }

    public void writeCursorDefinition(JavaWriter javaWriter) throws IOException {
        if(columnType == Column.FOREIGN_KEY) {
            //TODO: This is wrong, should be using condition query builder
            StringBuilder foreignKeys = new StringBuilder();
            for(int i = 0; i < foreignKeyReferences.length; i++) {
                ForeignKeyReference foreignKeyReference = foreignKeyReferences[i];

                foreignKeys.append(getModelStatement(columnFieldName + "." + foreignKeyReference.foreignColumnName()));

                if(i < foreignKeyReferences.length -1) {
                    foreignKeys.append(",");
                }
            }
            javaWriter.emitEmptyLine();
            javaWriter.emitSingleLineComment("Loading Model Foreign Key");
            javaWriter.emitStatement("ConditionQueryBuilder<%1s> conditionQueryBuilder = FlowManager.getPrimaryWhereQuery(%1s)", columnFieldType, columnFieldType + ".class");
            javaWriter.emitStatement(getModelStatement(columnFieldName) + " = new Select().from(%1s).where()\n" +
                    "                        .whereQuery(conditionQueryBuilder.replaceEmptyParams(%1s))\n" +
                    "                        .querySingle()", columnFieldType + ".class", foreignKeys.toString());
        } else {
            javaWriter.emitStatement(getModelStatement(columnFieldName) + " = %1s", getCursorStatement(columnFieldType, columnName));
        }
    }

    private static String getContentStatement(String columnName, String columnFieldName){
        return String.format("contentValues.put(\"%1s\", %1s)", columnName, getModelStatement(columnFieldName));
    }

    private static String getModelStatement(String columnFieldName) {
        return "model." + columnFieldName;
    }

    private static String getCursorStatement(String columnFieldType, String columnName) {
        String cursorMethod = LoadCursorWriter.CURSOR_METHOD_MAP.get(columnFieldType);
        return "cursor." + cursorMethod + "(cursor.getColumnIndex(\""  + columnName + "\"))";
    }


}
