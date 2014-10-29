package com.grosner.processor.model;

import com.google.common.collect.Sets;
import com.grosner.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class LoadCursorWriter implements FlowWriter {

    static final Map<String, String> CURSOR_METHOD_MAP = new HashMap<String, String>() {
        {
            put(byte[].class.getName(), "getBlob");
            put(Byte[].class.getName(), "getBlob");
            put(double.class.getName(), "getDouble");
            put(Double.class.getName(), "getDouble");
            put(float.class.getName(), "getFloat");
            put(Float.class.getName(), "getFloat");
            put(int.class.getName(), "getInt");
            put(Integer.class.getName(), "getInt");
            put(long.class.getName(), "getLong");
            put(Long.class.getName(), "getLong");
            put(short.class.getName(), "getShort");
            put(Short.class.getName(), "getShort");
            put(String.class.getName(), "getString");
        }
    };

    private TableDefinition tableDefinition;

    public LoadCursorWriter(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement(tableDefinition.modelClassName + " model = new " + tableDefinition.modelClassName + "()");
                for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitions) {
                    columnDefinition.writeCursorDefinition(javaWriter);
                }
                javaWriter.emitStatement("return model");
            }
        }, tableDefinition.modelClassName, "loadFromCursor", Sets.newHashSet(Modifier.PUBLIC), "Cursor", "cursor");
    }
}
