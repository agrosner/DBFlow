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
 * Description:
 */
public class DeleteWriter implements FlowWriter {

    private final TableDefinition tableDefinition;

    private final boolean isModelContainerAdapter;

    public DeleteWriter(TableDefinition tableDefinition, boolean isModelContainerAdapter) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
    }


    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        boolean shouldWrite = false;
        for (OneToManyDefinition oneToManyDefinition : tableDefinition.oneToManyDefinitions) {
            if(oneToManyDefinition.isDelete()) {
                shouldWrite = true;
                break;
            }
        }

        if(shouldWrite) {
            WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                @Override
                public void write(JavaWriter javaWriter) throws IOException {
                    for(OneToManyDefinition oneToManyDefinition: tableDefinition.oneToManyDefinitions) {
                        oneToManyDefinition.writeDelete(javaWriter);
                    }

                    javaWriter.emitStatement("super.delete(%1s)", ModelUtils.getVariable(isModelContainerAdapter));
                }
            }, "void", "delete", Sets.newHashSet(Modifier.PUBLIC, Modifier.FINAL), tableDefinition.getModelClassName(),
                                             ModelUtils.getVariable(isModelContainerAdapter));
        }
    }
}
