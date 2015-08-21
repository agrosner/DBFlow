package com.raizlabs.android.dbflow.processor.model.writer;

import com.raizlabs.android.dbflow.processor.model.builder.AdapterQueryBuilder;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.StatementMap;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

/**
 * Description:
 */
public class ContentValueModel implements FlowWriter {

    protected final ColumnAccessModel accessModel;

    private int index;

    private boolean isContentValues;

    private String databaseTypeName;

    private String putValue;

    public ContentValueModel(ColumnAccessModel accessModel, boolean isContentValues) {
        this.accessModel = accessModel;
        this.isContentValues = isContentValues;
    }

    public void setPutValue(String putValue) {
        this.putValue = putValue;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setDatabaseTypeName(String databaseTypeName) {
        this.databaseTypeName = databaseTypeName;
    }

    public boolean isContentValues() {
        return isContentValues;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        AdapterQueryBuilder contentValue = new AdapterQueryBuilder();

        boolean nullCheck;
        if(!isContentValues) {
            String statement = StatementMap.getStatement(SQLiteType.get(accessModel.castedClass));
            if(accessModel.isEnum) {
                statement = StatementMap.getStatement(SQLiteType.TEXT);
            }
            if (statement == null) {
                throw new RuntimeException(String.format("Writing insert statement failed for: %1s. A type converter" +
                                                    "must be defined for this type, or if this field is a Model, must be a foreign key definition.",
                                                    accessModel.castedClass));
            }
            nullCheck = (statement.equals("String") || statement.equals("Blob") ||
                                 accessModel.columnFieldBoxedType.equals(Boolean.class.getName())
                                 || !accessModel.isPrimitive || accessModel.isModelContainerAdapter);
        } else {
            nullCheck = accessModel.columnFieldBoxedType.equals(Boolean.class.getName()) ||
                        !accessModel.isPrimitive || accessModel.isModelContainerAdapter;
        }
        String accessStatement = accessModel.getQuery();
        // if statements of this type, we need to check for null :(
        boolean separateVariableForNullCheck = accessModel.requiresTypeConverter || accessModel.isModelContainerAdapter;
        if (nullCheck) {
            if(separateVariableForNullCheck) {
                AdapterQueryBuilder nullQueryBuilder = new AdapterQueryBuilder()
                        .append("Object model%1s = ");
                if(accessModel.requiresTypeConverter && !accessModel.isEnum) {
                    nullQueryBuilder.appendTypeConverter(null, databaseTypeName, false);
                }
                nullQueryBuilder.append(accessStatement);
                if(accessModel.requiresTypeConverter && !accessModel.isEnum) {
                    nullQueryBuilder.append(")");
                }
                javaWriter.emitStatement(nullQueryBuilder.getQuery(), accessModel.columnFieldName);
            }
            javaWriter.beginControlFlow("if (%1s != null) ", separateVariableForNullCheck
                    ? ("model" + accessModel.columnFieldName) : accessStatement);
        }


        if (!isContentValues) {
            String bindTypeLookup = accessModel.castedClass;
            // if enum we're treating it like a string
            if(accessModel.isEnum) {
                bindTypeLookup = String.class.getName();
            }
            contentValue.appendBindSQLiteStatement(index, bindTypeLookup);
        } else {
            contentValue.appendContentValues();
            contentValue.appendPut(putValue);
        }

        if(separateVariableForNullCheck) {
            contentValue.appendCast(accessModel.castedClass)
                        .append(("model" + accessModel.columnFieldName));
        } else {
            contentValue.append(accessStatement);
        }
        QueryBuilder query = contentValue.append(")");

        if (accessModel.isEnum) {
            contentValue.append(".name()");
        } else if (accessModel.isABlob && accessModel.isModelContainerAdapter) {
            contentValue.append(".getBlob()");
        }
        query.append(
                separateVariableForNullCheck ? ")" : "").getQuery();


        javaWriter.emitStatement(query.getQuery());

        if (nullCheck) {
            javaWriter.nextControlFlow("else");
            if(!isContentValues) {
                javaWriter.emitStatement("statement.bindNull(%1s)", index);
            } else {
                javaWriter.emitStatement("contentValues.putNull(\"%1s\")", putValue);
            }
            javaWriter.endControlFlow();
        }
    }
}
