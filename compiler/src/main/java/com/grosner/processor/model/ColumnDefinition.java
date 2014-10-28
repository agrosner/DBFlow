package com.grosner.processor.model;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.squareup.javawriter.JavaWriter;

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

    public ColumnDefinition(VariableElement element) {
        this.element = element;

        Column column = element.getAnnotation(Column.class);
        this.columnName = column.name();
        this.columnFieldName = element.getSimpleName().toString();
        this.columnFieldType = element.asType().toString();
        columnType = column.columnType();
    }


    @Override
    public String getFQCN() {
        return null;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitField("String", columnName.toUpperCase(),
                Sets.newHashSet(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL),
                "\""+columnName+"\"");
        javaWriter.emitEmptyLine();
    }

    public void writeContentValue(JavaWriter javaWriter) throws IOException {
        javaWriter.emitStatement("contentValues.put(%1s, %1s)");
    }
}
