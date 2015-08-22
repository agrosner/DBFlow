package com.raizlabs.android.dbflow.processor.utils;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.writer.LoadCursorWriter;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * Author: andrewgrosner
 * Description:
 */
public class ModelUtils {

    public static String getNewModelStatement(String modelClassName) {
        return modelClassName + " " + ModelUtils.getVariable(false) + " = new " + modelClassName + "()";
    }

    public static String getStaticMember(String classname, String fieldName) {
        return classname + "." + fieldName.toUpperCase();
    }

    public static String getContainerValueStatement(String columnFieldName) {
        return String.format("getValue(%1s)", "\"" + columnFieldName + "\"");
    }

    public static String getContainerStatement(String columnFieldName) {
        return String.format("modelContainer.%1s", getContainerValueStatement(columnFieldName));
    }

    public static String getModelStatement(String columnFieldName) {
        return "model." + columnFieldName;
    }

    public static String getColumnIndex(String columnName) {
        return "int index" + columnName + " = cursor.getColumnIndex(\"" + columnName + "\")";
    }

    public static String getCursorStatement(String columnFieldType, String columnName) {
        String cursorMethod = LoadCursorWriter.CURSOR_METHOD_MAP.get(columnFieldType);
        return "cursor." + cursorMethod + "(index" + columnName + ")";
    }

    public static String getFieldClass(String columnFieldType) {
        return columnFieldType + ".class";
    }

    public static String getClassFromAnnotation(ForeignKeyReference annotation) {
        String clazz = null;
        if (annotation != null) {
            try {
                annotation.columnType();
            } catch (MirroredTypeException mte) {
                clazz = mte.getTypeMirror().toString();
            }
        }
        return clazz;
    }

    public static String getClassFromAnnotation(ForeignKey annotation) {
        String clazz = null;
        if (annotation != null) {
            try {
                annotation.tableClass();
            } catch (MirroredTypeException mte) {
                clazz = mte.getTypeMirror().toString();
            }
        }
        return clazz;
    }

    public static TypeMirror getTypeMirrorFromAnnotation(ForeignKeyReference reference) {
        TypeMirror typeMirror = null;
        if (reference != null) {
            try {
                reference.columnType();
            } catch (MirroredTypeException mte) {
                typeMirror = mte.getTypeMirror();
            }
        }
        return typeMirror;
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

    public static String getUtils(boolean isModelContainer) {
        return isModelContainer ? ClassNames.MODEL_CONTAINER_UTILS : ClassNames.SQL_UTILS;
    }

    public static void writeColumnIndexCheckers(JavaWriter javaWriter,
                                                ForeignKeyReference[] foreignKeyReferences) throws IOException {

        QueryBuilder queryBuilder = new QueryBuilder("if ( ");
        for (int i = 0; i < foreignKeyReferences.length; i++) {
            ForeignKeyReference foreignKeyReference = foreignKeyReferences[i];
            queryBuilder.append("index")
                    .append(foreignKeyReference.columnName())
                    .append(" != -1")
                    .append(" && !cursor.isNull(index")
                    .append(foreignKeyReference.columnName())
                    .append(")");

            if (i < foreignKeyReferences.length - 1) {
                queryBuilder.appendSpaceSeparated("&&");
            }
        }
        queryBuilder.append(")");
        javaWriter.beginControlFlow(queryBuilder.getQuery());
    }
}
