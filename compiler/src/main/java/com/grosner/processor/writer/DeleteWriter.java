package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.model.ProcessorManager;
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

    private ProcessorManager manager;
    private TableDefinition tableDefinition;

    public DeleteWriter(ProcessorManager manager, TableDefinition tableDefinition) {

        this.manager = manager;
        this.tableDefinition = tableDefinition;
    }


    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("new Delete().from(%1s).where(getPrimaryWhereQuery(model)).query()", tableDefinition.tableName);
            }
        }, "void", "delete", Sets.newHashSet(Modifier.PUBLIC), tableDefinition.modelClassName, "model");
    }
}
