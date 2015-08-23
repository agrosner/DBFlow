package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.writer.ColumnAccessModel;
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
    private final boolean isModelContainerAdapter;

    public ExistenceWriter(BaseTableDefinition tableDefinition, boolean isModelContainerAdapter) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                if(tableDefinition instanceof TableDefinition && ((TableDefinition) tableDefinition).hasAutoIncrement) {
                    ColumnDefinition autoincrement = ((TableDefinition) tableDefinition).autoIncrementDefinition;
                    ColumnAccessModel accessModel = new ColumnAccessModel(tableDefinition.getManager(), autoincrement,
                                                                          isModelContainerAdapter);
                    String access =  accessModel.getQuery();
                    String accessNoCast =  accessModel.getQueryNoCast();
                    javaWriter.emitStatement("return %1s%1s > 0",
                            ((TableDefinition) tableDefinition).autoIncrementDefinition.columnFieldIsPrimitive ? "" : (accessNoCast + "!=null && "),
                            access);
                } else {
                    javaWriter.emitStatement("return new Select().from(%1s).where(getPrimaryModelWhere(%1s)).hasData()",
                            ModelUtils.getFieldClass(tableDefinition.getModelClassName()), ModelUtils.getVariable(
                            isModelContainerAdapter));
                }
            }
        }, "boolean", "exists", Sets.newHashSet(Modifier.PUBLIC), ModelUtils.getParameter(isModelContainerAdapter,tableDefinition.getModelClassName()),
                ModelUtils.getVariable(isModelContainerAdapter));
    }
}
