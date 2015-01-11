package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Description: Writes the statement if Model or ModelContainer exists.
 */
public class ExistenceWriter implements FlowWriter {

    private final BaseTableDefinition tableDefinition;
    private final boolean isModelContainer;

    public ExistenceWriter(BaseTableDefinition tableDefinition, boolean isModelContainer) {
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
                        ModelUtils.getFieldClass(tableDefinition.getModelClassName()), ModelUtils.getVariable(isModelContainer));
            }
        }, "boolean", "exists", Sets.newHashSet(Modifier.PUBLIC), ModelUtils.getParameter(isModelContainer,tableDefinition.getModelClassName()),
                ModelUtils.getVariable(isModelContainer));
    }
}
