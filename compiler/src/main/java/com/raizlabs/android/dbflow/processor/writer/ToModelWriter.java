package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
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
    private boolean isModelContainerDefinition;

    public ToModelWriter(TableDefinition tableDefinition, boolean isModelContainerDefinition) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerDefinition = isModelContainerDefinition;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement(tableDefinition.getModelClassName() + " " + ModelUtils.getVariable(false) +
                        " = new " + tableDefinition.getModelClassName() + "()");
                for (ColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {
                    columnDefinition.writeToModelDefinition(javaWriter);
                }
                javaWriter.emitStatement("return " + ModelUtils.getVariable(false));
            }
        }, tableDefinition.getModelClassName(), "toModel", Sets.newHashSet(Modifier.PUBLIC),
                ModelUtils.getParameter(true, tableDefinition.getModelClassName()),
                ModelUtils.getVariable(true));
    }
}
