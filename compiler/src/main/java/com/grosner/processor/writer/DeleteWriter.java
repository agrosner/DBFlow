package com.grosner.processor.writer;

import com.google.common.collect.Sets;
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
                javaWriter.beginControlFlow("if(%1s)", "async");
                WriterUtils.emitTransactionManagerCall(javaWriter, "delete", ModelUtils.getVariable(isModelContainer));
                javaWriter.nextControlFlow("else");
                javaWriter.emitStatement(" new Delete().from(%1s).where(getPrimaryModelWhere(%1s)).query()",
                        ModelUtils.getFieldClass(tableDefinition.tableName), ModelUtils.getVariable(isModelContainer));
                javaWriter.endControlFlow();
            }
        }, "void", "delete", Sets.newHashSet(Modifier.PUBLIC),
                "boolean", "async",
                ModelUtils.getParameter(isModelContainer, tableDefinition.modelClassName),
                ModelUtils.getVariable(isModelContainer));
    }
}
