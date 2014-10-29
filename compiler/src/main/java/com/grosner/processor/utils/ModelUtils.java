package com.grosner.processor.utils;

import com.grosner.processor.writer.LoadCursorWriter;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelUtils {

    public static String getStaticMember(String classname, String fieldName) {
        return new StringBuilder(classname).append(".").append(fieldName.toUpperCase()).toString();
    }


    public static String getContentStatement(String columnName, String columnFieldName){
        return String.format("contentValues.put(\"%1s\", %1s)", columnName, getModelStatement(columnFieldName));
    }

    public static String getModelStatement(String columnFieldName) {
        return "model." + columnFieldName;
    }

    public static String getCursorStatement(String columnFieldType, String columnName) {
        String cursorMethod = LoadCursorWriter.CURSOR_METHOD_MAP.get(columnFieldType);
        return "cursor." + cursorMethod + "(cursor.getColumnIndex(\""  + columnName + "\"))";
    }

    public static String getFieldClass(String columnFieldType) {
        return columnFieldType + ".class";
    }
}
