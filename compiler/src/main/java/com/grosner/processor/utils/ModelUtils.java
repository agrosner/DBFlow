package com.grosner.processor.utils;

import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.processor.writer.LoadCursorWriter;

import javax.lang.model.type.MirroredTypeException;
import java.lang.annotation.Annotation;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelUtils {

    public static String getNewModelStatement(String modelClassName) {
        return modelClassName + " " + ModelUtils.getVariable(false) + " = new " + modelClassName + "()";

    }

    public static String getStaticMember(String classname, String fieldName) {
        return new StringBuilder(classname).append(".").append(fieldName.toUpperCase()).toString();
    }


    public static String getContentStatement(String columnName, String columnFieldName, String columnFieldType, boolean isContainer){
        return String.format("contentValues.put(\"%1s\", %1s)", columnName, getCastedValue(columnFieldType,
                getVariableAccessStatement(columnFieldName, isContainer)));
    }

    public static String getVariableAccessStatement(String columnFieldName, boolean isModelContainer) {
        return isModelContainer ?
                getContainerStatement(columnFieldName) : getModelStatement(columnFieldName);
    }

    public static String getVariableSetterStatement(String columnFieldName, String columnFieldType, boolean isModelContainer) {
        return isModelContainer ?
                getContainerSetterStatement(columnFieldName, columnFieldType) : getModelSetterStatement(columnFieldName, columnFieldType);
    }

    public static String getContainerSetterStatementRaw(String columnFieldName, String rawColumnExpression) {
        return String.format("modelContainer.put(\"%1s\", %1s)", columnFieldName, rawColumnExpression);
    }

    public static String getContainerSetterStatement(String columnFieldName, String columnFieldType) {
        return getContainerSetterStatementRaw(columnFieldName, getCursorStatement(columnFieldType, columnFieldName));
    }

    public static String getContainerStatement(String columnFieldName) {
        return String.format("modelContainer.getValue(%1s)",  "\""+columnFieldName+"\"");
    }

    public static String getModelStatement(String columnFieldName) {
        return "model." + columnFieldName;
    }

    public static String getModelSetterStatement(String columnFieldName, String columnFieldType) {
        return String.format("%1s = %1s", getModelStatement(columnFieldName), getCursorStatement(columnFieldType, columnFieldName));
    }

    public static String getCursorStatement(String columnFieldType, String columnName) {
        String cursorMethod = LoadCursorWriter.CURSOR_METHOD_MAP.get(columnFieldType);
        return "cursor." + cursorMethod + "(cursor.getColumnIndex(\""  + columnName + "\"))";
    }

    public static String getFieldClass(String columnFieldType) {
        return columnFieldType + ".class";
    }

    public static String getClassFromAnnotation(ForeignKeyReference annotation) {
        String clazz = null;
        if(annotation != null) {
            try {
                annotation.columnType();
            } catch (MirroredTypeException mte) {
                clazz = mte.getTypeMirror().toString();
            }
        }
        return clazz;
    }

    public static String getCastedValue(String columnFieldType, String statement) {
        return String.format("((%1s)%1s)", columnFieldType, statement);
    }

    public static String getVariable(boolean isModelContainer) {
        return isModelContainer ? "modelContainer" : "model";
    }

    public static String getParameter(boolean isModelContainer, String modelClassName) {
        return isModelContainer ? "ModelContainer<" + modelClassName + ", ?>" : modelClassName;

    }
}
