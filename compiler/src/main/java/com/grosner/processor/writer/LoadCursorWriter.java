package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.processor.definition.ColumnDefinition;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.utils.ModelUtils;
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

    public static final Map<String, String> CURSOR_METHOD_MAP = new HashMap<String, String>() {
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
    private final boolean isModelContainer;

    public LoadCursorWriter(TableDefinition tableDefinition, boolean isModelContainer) {
        this.tableDefinition = tableDefinition;
        this.isModelContainer = isModelContainer;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        String[] params = new String[isModelContainer ? 4: 2];
        params[0] = "Cursor";
        params[1] = "cursor";
        if(isModelContainer) {
            params[2] = ModelUtils.getParameter(true, tableDefinition.modelClassName);
            params[3] = ModelUtils.getVariable(true);
        }
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                if(!isModelContainer) {
                    javaWriter.emitStatement(ModelUtils.getNewModelStatement(tableDefinition.modelClassName));
                }

                for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitions) {
                    columnDefinition.writeCursorDefinition(javaWriter, isModelContainer);
                }

                if(!isModelContainer) {
                    javaWriter.emitStatement("return model");
                }
            }
        }, isModelContainer ? "void" : tableDefinition.modelClassName, "loadFromCursor", Sets.newHashSet(Modifier.PUBLIC), params);
    }
}
