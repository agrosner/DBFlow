package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.processor.definition.ColumnDefinition;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ToModelWriter implements FlowWriter {

    private TableDefinition tableDefinition;

    public ToModelWriter(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement(tableDefinition.modelClassName + " " + ModelUtils.getVariable(false) +
                        " = new " + tableDefinition.modelClassName + "()");
                for (ColumnDefinition columnDefinition : tableDefinition.columnDefinitions) {
                    columnDefinition.writeToModelDefinition(javaWriter);
                }
                javaWriter.emitStatement("return " + ModelUtils.getVariable(false));
            }
        }, tableDefinition.modelClassName, "toModel", Sets.newHashSet(Modifier.PUBLIC),
                ModelUtils.getParameter(true, tableDefinition.modelClassName),
                ModelUtils.getVariable(true));
    }
}
