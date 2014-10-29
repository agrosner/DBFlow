package com.grosner.processor.model;

import com.google.common.collect.Sets;
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

    public ExistenceWriter(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return new Select().from(%1s).where(getPrimaryModelWhere(%1s)).hasData()",
                        tableDefinition.modelClassName + ".class", "model");
            }
        }, "boolean", "exists", Sets.newHashSet(Modifier.PUBLIC), tableDefinition.modelClassName, "model");
    }
}
