package com.raizlabs.android.dbflow.processor.model.writer;

import com.raizlabs.android.dbflow.processor.model.builder.AdapterQueryBuilder;
import com.raizlabs.android.dbflow.processor.writer.FlowWriter;
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

        boolean nullCheck = !isContentValues;
        if (nullCheck) {
            String statement = StatementMap.getStatement(SQLiteType.get(accessModel.castedClass));
            if (statement == null) {
                throw new IOException(String.format("Writing insert statement failed for: %1s. A type converter" +
                                                    "must be defined for this type, or if this field is a Model, must be a foreign key definition.",
                                                    accessModel.castedClass));
            }
            nullCheck = (statement.equals("String") || statement.equals("Blob") || !accessModel.isPrimitive);
        } else {
            contentValue.appendContentValues();
            contentValue.appendPut(putValue);
        }
        String accessStatement = accessModel.getQuery();
        // if statements of this type, we need to check for null :(
        if (nullCheck) {
            javaWriter.beginControlFlow("if (%1s != null) ", accessStatement);
        }

        if (!isContentValues) {
            contentValue.appendBindSQLiteStatement(index, accessModel.castedClass);
        }

        if (accessModel.requiresTypeConverter) {
            contentValue.appendTypeConverter(accessModel.castedClass, databaseTypeName, false);
        }
        String query = contentValue.append(accessStatement).append(")").append(
                accessModel.requiresTypeConverter ? "))" : "").getQuery();


        javaWriter.emitStatement(query);

        if (nullCheck) {
            javaWriter.nextControlFlow("else");
            javaWriter.emitStatement("statement.bindNull(%1s)", index);
            javaWriter.endControlFlow();
        }
    }
}
