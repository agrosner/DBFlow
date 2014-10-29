package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.processor.definition.ColumnDefinition;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ContentValuesWriter implements FlowWriter {

    private TableDefinition tableDefinition;

    public ContentValuesWriter(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("ContentValues contentValues = new ContentValues()");
                for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitions) {
                    columnDefinition.writeContentValue(javaWriter);
                }
                javaWriter.emitEmptyLine();

                javaWriter.emitStatement("SqlUtils.save(%1s, \"%1s\", %1s, %1s, %1s)", "async", tableDefinition.tableName, "model", "contentValues", "saveMode");
            }
        }, "void", "save", Sets.newHashSet(Modifier.PUBLIC), "boolean", "async", tableDefinition.modelClassName, "model", "int", "saveMode");
    }
}
