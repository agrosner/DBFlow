package com.grosner.processor.model;

import com.google.common.collect.Sets;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ContentValuesWriter implements FlowWriter {

    private ArrayList<ColumnDefinition> columnDefinitions;

    private String modelClassName;

    private String tableName;

    public ContentValuesWriter(String tableName, String modelClassName, ArrayList<ColumnDefinition> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
        this.modelClassName = modelClassName;
        this.tableName = tableName;
    }
    @Override
    public String getFQCN() {
        return null;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {


        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        javaWriter.beginMethod("void", "save", Sets.newHashSet(Modifier.PUBLIC), modelClassName, "model", "int", "saveMode");
        javaWriter.emitStatement("ContentValues contentValues = new ContentValues()");
        for(ColumnDefinition columnDefinition: columnDefinitions) {
            javaWriter.emitStatement("contentValues.put(\"%1s\", %1s)", columnDefinition.columnName, "model." + columnDefinition.columnFieldName);
        }

        javaWriter.emitStatement("SqlUtils.save(\"%1s\", %1s, %1s, %1s)", tableName, "model", "contentValues", "saveMode");

        javaWriter.endMethod();

        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        javaWriter.beginMethod("void", "loadFromCursor", Sets.newHashSet(Modifier.PUBLIC), "Cursor", "cursor");
        javaWriter.endMethod();
    }
}
