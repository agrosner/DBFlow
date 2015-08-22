package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.definition.OneToManyDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

import javax.lang.model.element.Modifier;

/**
 * Description: Overrides the save, update, and insert methods if the {@link com.raizlabs.android.dbflow.annotation.OneToMany.Method#SAVE} is used.
 */
public class OneToManySaveWriter implements FlowWriter {

    private final TableDefinition tableDefinition;
    private final boolean isModelContainerAdapter;

    public OneToManySaveWriter(TableDefinition tableDefinition, boolean isModelContainerAdapter) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {

        boolean shouldWrite = false;
        for (OneToManyDefinition oneToManyDefinition : tableDefinition.oneToManyDefinitions) {
            if(oneToManyDefinition.isSave()) {
                shouldWrite = true;
                break;
            }
        }

        if(shouldWrite) {
            writeMethod("save", javaWriter);
            writeMethod("insert", javaWriter);
            writeMethod("update", javaWriter);
        }
    }

    private void writeMethod(final String methodName, JavaWriter javaWriter) {
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                    @Override
                    public void write(JavaWriter javaWriter) throws IOException {
                        for (OneToManyDefinition oneToManyDefinition : tableDefinition.oneToManyDefinitions) {
                            oneToManyDefinition.writeSave(javaWriter);
                        }

                        javaWriter.emitStatement("super.%1s(%1s)", methodName, ModelUtils.getVariable(isModelContainerAdapter));
                    }
                }, "void", methodName, Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), tableDefinition.getModelClassName(),
                ModelUtils.getVariable(isModelContainerAdapter));
    }
}
