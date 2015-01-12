package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

import javax.lang.model.element.Modifier;

/**
 * Description: Handles writing the delete statement for a ModelAdapter
 */
public class DeleteWriter implements FlowWriter {

    private final boolean isModelContainer;
    private TableDefinition tableDefinition;

    public DeleteWriter(TableDefinition tableDefinition, boolean isModelContainer) {
        this.isModelContainer = isModelContainer;
        this.tableDefinition = tableDefinition;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
                    @Override
                    public void write(JavaWriter javaWriter) throws IOException {
                        javaWriter.emitStatement("%1s.delete(%1s, this, %1s)",
                                ModelUtils.getUtils(isModelContainer),
                                ModelUtils.getVariable(isModelContainer), "async");
                    }
                }, "void", "delete", Sets.newHashSet(Modifier.PUBLIC),
                "boolean", "async",
                ModelUtils.getParameter(isModelContainer, tableDefinition.getModelClassName()),
                ModelUtils.getVariable(isModelContainer));
    }
}
