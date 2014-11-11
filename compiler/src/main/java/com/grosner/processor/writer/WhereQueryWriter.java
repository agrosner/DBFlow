package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.processor.definition.BaseTableDefinition;
import com.grosner.processor.definition.ColumnDefinition;
import com.grosner.processor.definition.ModelViewDefinition;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.model.builder.MockConditionQueryBuilder;
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
public class WhereQueryWriter implements FlowWriter {

    private BaseTableDefinition tableDefinition;
    private final boolean isModelContainer;

    public WhereQueryWriter(BaseTableDefinition tableDefinition, boolean isModelContainer) {
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
                MockConditionQueryBuilder conditionQueryBuilder = new MockConditionQueryBuilder("return ");
                conditionQueryBuilder.appendCreation(tableDefinition.getModelClassName());

                int primaryColumnSize = tableDefinition.getPrimaryColumnDefinitions().size();
                for (int i = 0; i < primaryColumnSize; i++) {
                    ColumnDefinition columnDefinition = tableDefinition.getPrimaryColumnDefinitions().get(i);
                    conditionQueryBuilder.appendMockCondition(ModelUtils.getStaticMember(tableDefinition.getTableSourceClassName(), columnDefinition.columnName),
                            ModelUtils.getAccessStatement(columnDefinition.columnName, columnDefinition.columnFieldType,
                                    columnDefinition.columnFieldName, isModelContainer, false, false, columnDefinition.hasTypeConverter));
                    if(i < tableDefinition.getPrimaryColumnDefinitions().size()-1) {
                        conditionQueryBuilder.append(",");
                    }
                }
                conditionQueryBuilder.appendEndCreation();
                javaWriter.emitStatement(conditionQueryBuilder.getQuery());

            }
        }, "ConditionQueryBuilder<" + tableDefinition.getModelClassName() + ">", "getPrimaryModelWhere", Sets.newHashSet(Modifier.PUBLIC),
                ModelUtils.getParameter(isModelContainer ,tableDefinition.getModelClassName()),
                ModelUtils.getVariable(isModelContainer));

        // Don't write empty statement for Model Container
        if(!isModelContainer && !(tableDefinition instanceof ModelViewDefinition)) {
            final TableDefinition definition = ((TableDefinition) tableDefinition);
            javaWriter.emitEmptyLine();
            javaWriter.emitAnnotation(Override.class);
            WriterUtils.emitMethod(javaWriter, new FlowWriter() {
                @Override
                public void write(JavaWriter javaWriter) throws IOException {
                    MockConditionQueryBuilder conditionQueryBuilder = new MockConditionQueryBuilder("return ");
                    conditionQueryBuilder.appendCreation(tableDefinition.getModelClassName());
                    for (int i = 0; i < definition.primaryColumnDefinitions.size(); i++) {
                        ColumnDefinition columnDefinition = definition.primaryColumnDefinitions.get(i);
                        conditionQueryBuilder.appendMockCondition(definition.definitionClassName + "." + columnDefinition.columnName.toUpperCase(), "\"?\"");

                        if(i < definition.primaryColumnDefinitions.size()-1) {
                            conditionQueryBuilder.append(",");
                        }
                    }

                    conditionQueryBuilder.appendEndCreation();
                    javaWriter.emitStatement(conditionQueryBuilder.getQuery());
                }
            }, "ConditionQueryBuilder<" + tableDefinition.getModelClassName() + ">", "createPrimaryModelWhere", Sets.newHashSet(Modifier.PUBLIC));
        }
    }
}
