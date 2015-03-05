package com.raizlabs.android.dbflow.processor.utils;

import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.model.builder.AdapterQueryBuilder;
import com.raizlabs.android.dbflow.processor.writer.LoadCursorWriter;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.StatementMap;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
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
        return new StringBuilder(classname).append(".").append(fieldName.toUpperCase()).toString();
    }

    public static void writeContentValueStatement(JavaWriter javaWriter, boolean isContentValues,
                                                  int index, String putValue, String localColumnName,
                                                  String castedClass, String foreignColumnName,
                                                  String containerKeyName,
                                                  boolean isContainer, boolean isModelContainer,
                                                  boolean isForeignKey,
                                                  boolean requiresTypeConverter, String databaseTypeName,
                                                  boolean isColumnPrimitive) throws IOException {
        AdapterQueryBuilder contentValue = new AdapterQueryBuilder();

        boolean nullCheck = !isContentValues;
        if(nullCheck) {
            String statement = StatementMap.getStatement(SQLiteType.get(castedClass));
            if(statement == null) {
                throw new IOException(String.format("Writing insert statement failed for: %1s. A type converter" +
                        "must be defined for this type, or if this field is a Model, must be a foreign key definition.", castedClass));
            }
            nullCheck = (statement.equals("String") || statement.equals("Blob") || !isColumnPrimitive);
        } else {
            contentValue.appendContentValues();
            contentValue.appendPut(putValue);
        }
        String accessStatement = getAccessStatement(localColumnName, castedClass,
                foreignColumnName, containerKeyName, isContainer, isModelContainer, isForeignKey, requiresTypeConverter);
        // if statements of this type, we need to check for null :(
        if(nullCheck) {
            javaWriter.beginControlFlow("if (%1s != null) ", accessStatement);
        }

        if(!isContentValues) {
            contentValue.appendBindSQLiteStatement(index, castedClass);
        }

        if (requiresTypeConverter) {
            contentValue.appendTypeConverter(castedClass, databaseTypeName, false);
        }
        String query = contentValue.append(accessStatement).append(")").append(requiresTypeConverter ? "))" : "").getQuery();



        javaWriter.emitStatement(query);

        if(nullCheck) {
            javaWriter.nextControlFlow("else");
            javaWriter.emitStatement("statement.bindNull(%1s)", index);
            javaWriter.endControlFlow();
        }
    }

    public static String getAccessStatement(String localColumnName,
                                            String castedClass, String foreignColumnName, String containerKeyName,
                                            boolean isContainer, boolean isModelContainer, boolean isForeignKey, boolean requiresTypeConverter) {
        AdapterQueryBuilder contentValue = new AdapterQueryBuilder();

        if (!requiresTypeConverter) {
            contentValue.appendCast(castedClass);
        }
        contentValue.appendVariable(isContainer).append(".");
        if (isContainer) {
            contentValue.appendGetValue(containerKeyName);
        } else if (isModelContainer) {
            contentValue.append(localColumnName)
                    .append(".")
                    .appendGetValue(foreignColumnName);
        } else {
            if (isForeignKey) {
                contentValue.append(localColumnName)
                        .append(".");
            }
            contentValue.append(foreignColumnName);
        }

        if (!requiresTypeConverter) {
            contentValue.append(")");
        }
        return contentValue.getQuery();
    }

    public static void writeLoadFromCursorDefinitionField(JavaWriter javaWriter, ProcessorManager processorManager, String columnFieldType, String columnFieldName, String columnName,
                                                          String foreignColumnName, String containerKeyName,
                                                          Element modelType, boolean hasTypeConverter, boolean isModelContainerDefinition, boolean isFieldModelContainer,
                                                          boolean isNullable) throws IOException {
        AdapterQueryBuilder queryBuilder = new AdapterQueryBuilder().appendVariable(isModelContainerDefinition);
        if (isFieldModelContainer) {
            queryBuilder.append(".").append(columnFieldName);
        }
        if (isModelContainerDefinition) {
            queryBuilder.appendPut(containerKeyName);
        } else if (isFieldModelContainer) {
            queryBuilder.appendPut(foreignColumnName);
        } else {
            queryBuilder.append(".").append(columnFieldName).appendSpaceSeparated("=");
        }

        // TODO: find a way to convert a type properly
        String newFieldType = null;
        if (hasTypeConverter) {
            TypeConverterDefinition typeConverterDefinition = processorManager.getTypeConverterDefinition((TypeElement) modelType);
            if (typeConverterDefinition != null) {
                newFieldType = typeConverterDefinition.getDbElement().asType().toString();
            }
        } else {
            newFieldType = columnFieldType;
        }

        javaWriter.emitStatement(getColumnIndex(columnName));
        String index = "index" + columnName;
        javaWriter.beginControlFlow("if (%1s != -1) ", index);

        String cursorStatment = ModelUtils.getCursorStatement(newFieldType, columnName);
        AdapterQueryBuilder nullableQueryBuilder = new AdapterQueryBuilder(queryBuilder.getQuery());
        if (hasTypeConverter && !isModelContainerDefinition) {
            queryBuilder.appendTypeConverter(columnFieldType, columnFieldType, true);
            nullableQueryBuilder.appendTypeConverter(columnFieldType, columnFieldType, true);
        }

        queryBuilder.append(cursorStatment);
        nullableQueryBuilder.append("null");

        if (hasTypeConverter && !isModelContainerDefinition) {
            queryBuilder.append("))");
            nullableQueryBuilder.append("))");
        } else if (isModelContainerDefinition || isFieldModelContainer) {
            queryBuilder.append(")");
            nullableQueryBuilder.append(")");
        }

        if (isNullable) {
            javaWriter.beginControlFlow("if (cursor.isNull(%1s)) ", index);
            javaWriter.emitStatement(nullableQueryBuilder.getQuery());
            javaWriter.nextControlFlow(" else ");
        }
        javaWriter.emitStatement(queryBuilder.getQuery());
        if (isNullable) {
            javaWriter.endControlFlow();
        }
        javaWriter.endControlFlow();
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
        return isModelContainer ? Classes.MODEL_CONTAINER_UTILS : Classes.SQL_UTILS;
    }

    public static void writeColumnIndexCheckers(JavaWriter javaWriter, ForeignKeyReference[] foreignKeyReferences) throws IOException {

        for (ForeignKeyReference foreignKeyReference : foreignKeyReferences) {
            javaWriter.emitStatement(ModelUtils.getColumnIndex(foreignKeyReference.columnName()));
        }

        QueryBuilder queryBuilder = new QueryBuilder("if ( ");
        for (int i = 0; i < foreignKeyReferences.length; i++) {
            ForeignKeyReference foreignKeyReference = foreignKeyReferences[i];
            queryBuilder.append("index")
                    .append(foreignKeyReference.columnName())
                    .append(" != -1")
                    .append(" && !cursor.isNull(index")
                    .append(foreignKeyReference.columnName())
                    .append(")");

            if(i < foreignKeyReferences.length - 1) {
                queryBuilder.appendSpaceSeparated("&&");
            }
        }
        queryBuilder.append(")");
        javaWriter.beginControlFlow(queryBuilder.getQuery());
    }
}
