package com.grosner.processor.utils;

import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.processor.Classes;
import com.grosner.processor.definition.TypeConverterDefinition;
import com.grosner.processor.model.ProcessorManager;
import com.grosner.processor.model.builder.AdapterQueryBuilder;
import com.grosner.processor.writer.LoadCursorWriter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

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

    /**
     *
     * @param putValue
     * @param localColumnName
     * @param castedClass
     * @param foreignColumnName
     * @param isContainer If class itself is a {@link com.grosner.dbflow.annotation.ContainerAdapter}
     * @param isModelContainer If field it's accessing is a model container
     * @return
     */
    public static String getContentValueStatement(String putValue, String localColumnName,
                                                  String castedClass, String foreignColumnName,
                                                  boolean isContainer, boolean isModelContainer,
                                                  boolean isForeignKey,
                                                  boolean requiresTypeConverter, String databaseTypeName) {
        AdapterQueryBuilder contentValue = new AdapterQueryBuilder();
        contentValue.appendContentValues();
        contentValue.appendPut(putValue);
        String accessStatement = getAccessStatement(localColumnName, castedClass,
                foreignColumnName, isContainer, isModelContainer, isForeignKey, requiresTypeConverter);
        if(requiresTypeConverter) {
            contentValue.appendTypeConverter(castedClass, databaseTypeName, false);
        }
        return contentValue.append(accessStatement).append(")").append(requiresTypeConverter ? "))" : "").getQuery();
    }

    /**
     *
     * @param localColumnName
     * @param castedClass
     * @param foreignColumnName
     * @param isContainer If class itself is a {@link com.grosner.dbflow.annotation.ContainerAdapter}
     * @param isModelContainer If field it's accessing is a model container
     * @param requiresTypeConverter
     * @return
     */
    public static String getAccessStatement(String localColumnName,
                                            String castedClass, String foreignColumnName,
                                            boolean isContainer, boolean isModelContainer, boolean isForeignKey, boolean requiresTypeConverter) {
        AdapterQueryBuilder contentValue = new AdapterQueryBuilder();

        if(!requiresTypeConverter) {
            contentValue.appendCast(castedClass);
        }
        contentValue.appendVariable(isContainer).append(".");
        if(isContainer) {
            contentValue.appendGetValue(foreignColumnName);
        } else if(isModelContainer) {
            contentValue.append(localColumnName)
                    .append(".")
                    .appendGetValue(foreignColumnName);
        } else {
            if(isForeignKey) {
                contentValue.append(localColumnName)
                        .append(".");
            }
            contentValue.append(foreignColumnName);
        }

        if(!requiresTypeConverter) {
            contentValue.append(")");
        }
        return contentValue.getQuery();
    }

    public static String getLoadFromCursorDefinitionField(ProcessorManager processorManager, String columnFieldType, String columnFieldName, String columnName,
                                                          String foreignColumnName,
                                                          TypeElement modelType, boolean hasTypeConverter, boolean isModelContainerDefinition, boolean isFieldModelContainer) {
        AdapterQueryBuilder queryBuilder = new AdapterQueryBuilder().appendVariable(isModelContainerDefinition);
        if(isFieldModelContainer) {
            queryBuilder.append(".").append(columnFieldName);
        }
        if(isModelContainerDefinition) {
            queryBuilder.appendPut(columnFieldName);
        } else if(isFieldModelContainer) {
            queryBuilder.appendPut(foreignColumnName);
        } else {
            queryBuilder.append(".").append(columnFieldName).appendSpaceSeparated("=");
        }

        // TODO: find a way to convert a type properly
        String newFieldType;
        if(hasTypeConverter) {
            TypeConverterDefinition typeConverterDefinition = processorManager.getTypeConverterDefinition(modelType);
            newFieldType = typeConverterDefinition.getDbElement().asType().toString();
        } else {
            newFieldType = columnFieldType;
        }
        String cursorStatment = ModelUtils.getCursorStatement(newFieldType, columnName);
        if(hasTypeConverter && !isModelContainerDefinition) {
            queryBuilder.appendTypeConverter(columnFieldType, columnFieldType, true);
        }

        queryBuilder.append(cursorStatment);
        if(hasTypeConverter && !isModelContainerDefinition) {
            queryBuilder.append("))");
        } else if(isModelContainerDefinition || isFieldModelContainer) {
            queryBuilder.append(")");
        }

        return queryBuilder.getQuery();
    }

    public static String getContainerValueStatement(String columnFieldName) {
        return String.format("getValue(%1s)",  "\""+columnFieldName+"\"");
    }

    public static String getContainerStatement(String columnFieldName) {
        return String.format("modelContainer.%1s",  getContainerValueStatement(columnFieldName));
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

    public static String getUtils(boolean isModelContainer) {
        return isModelContainer ? Classes.MODEL_CONTAINER_UTILS : Classes.SQL_UTILS;
    }
}
