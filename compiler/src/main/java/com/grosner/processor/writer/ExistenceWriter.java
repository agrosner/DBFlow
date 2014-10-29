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
public class ExistenceWriter implements FlowWriter {

    private final TableDefinition tableDefinition;
    private final boolean isModelContainer;

    public ExistenceWriter(TableDefinition tableDefinition, boolean isModelContainer) {
        this.tableDefinition = tableDefinition;
        this.isModelContainer = isModelContainer;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return new Select().from(%1s).where(getPrimaryModelWhere(%1s)).hasData()",
                        ModelUtils.getFieldClass(tableDefinition.modelClassName), ModelUtils.getVariable(isModelContainer));
            }
        }, "boolean", "exists", Sets.newHashSet(Modifier.PUBLIC), ModelUtils.getParameter(isModelContainer,tableDefinition.modelClassName),
                ModelUtils.getVariable(isModelContainer));
    }
}
