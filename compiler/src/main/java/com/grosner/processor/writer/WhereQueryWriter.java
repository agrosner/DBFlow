package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.processor.definition.BaseTableDefinition;
import com.grosner.processor.definition.ColumnDefinition;
import com.grosner.processor.definition.ModelViewDefinition;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.model.builder.MockConditionQueryBuilder;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;

import javax.lang.model.element.Modifier;

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
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                    @Override
                    public void write(JavaWriter javaWriter) throws IOException {
                        MockConditionQueryBuilder conditionQueryBuilder = new MockConditionQueryBuilder("return ");
                        conditionQueryBuilder.appendCreation(tableDefinition.getModelClassName());

                        int primaryColumnSize = tableDefinition.getPrimaryColumnDefinitions().size();
                        if (primaryColumnSize > 0) {
                            for (int i = 0; i < primaryColumnSize; i++) {
                                ColumnDefinition columnDefinition = tableDefinition.getPrimaryColumnDefinitions().get(i);
                                conditionQueryBuilder.appendMockCondition(ModelUtils.getStaticMember(tableDefinition.getTableSourceClassName(), columnDefinition.columnName),
                                        ModelUtils.getAccessStatement(columnDefinition.columnName, columnDefinition.columnFieldType,
                                                columnDefinition.columnFieldName, columnDefinition.containerKeyName, isModelContainer, false, false, columnDefinition.hasTypeConverter));
                                if (i < tableDefinition.getPrimaryColumnDefinitions().size() - 1) {
                                    conditionQueryBuilder.append(",");
                                }
                            }
                        } else if (!(tableDefinition instanceof ModelViewDefinition)) {
                            ColumnDefinition autoIncrementDefinition = ((TableDefinition) tableDefinition).autoIncrementDefinition;
                            if (autoIncrementDefinition != null) {
                                conditionQueryBuilder.appendMockCondition(ModelUtils.getStaticMember(tableDefinition.getTableSourceClassName(), autoIncrementDefinition.columnName),
                                        ModelUtils.getAccessStatement(autoIncrementDefinition.columnName, autoIncrementDefinition.columnFieldType,
                                                autoIncrementDefinition.columnFieldName, autoIncrementDefinition.containerKeyName, isModelContainer, false, false, autoIncrementDefinition.hasTypeConverter));
                            }
                        }
                        conditionQueryBuilder.appendEndCreation();
                        javaWriter.emitStatement(conditionQueryBuilder.getQuery());

                    }
                }, "ConditionQueryBuilder<" + tableDefinition.getModelClassName() + ">", "getPrimaryModelWhere", Sets.newHashSet(Modifier.PUBLIC),
                ModelUtils.getParameter(isModelContainer, tableDefinition.getModelClassName()),
                ModelUtils.getVariable(isModelContainer));

        // Don't write empty statement for Model Container
        if (!isModelContainer && !(tableDefinition instanceof ModelViewDefinition)) {
            final TableDefinition definition = ((TableDefinition) tableDefinition);
            WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                        @Override
                        public void write(JavaWriter javaWriter) throws IOException {
                            MockConditionQueryBuilder conditionQueryBuilder = new MockConditionQueryBuilder("return ");
                            conditionQueryBuilder.appendCreation(tableDefinition.getModelClassName());

                            int columnSize = tableDefinition.getColumnDefinitions().size();
                            if (columnSize > 0) {
                                for (int i = 0; i < columnSize; i++) {
                                    ColumnDefinition columnDefinition = definition.getColumnDefinitions().get(i);
                                    if(columnDefinition.columnType == Column.FOREIGN_KEY) {
                                        boolean isFirst = true;
                                        for(ForeignKeyReference reference: columnDefinition.foreignKeyReferences) {
                                            conditionQueryBuilder.appendMockCondition(definition.definitionClassName + "." + (columnDefinition.columnName + "_" + reference.columnName()).toUpperCase(), "\"?\"");
                                            if(!isFirst) {
                                                conditionQueryBuilder.append(",");
                                            } else {
                                                isFirst = false;
                                            }
                                        }
                                    } else {
                                        conditionQueryBuilder.appendMockCondition(definition.definitionClassName + "." + columnDefinition.columnName.toUpperCase(), "\"?\"");
                                    }

                                    if (i < columnSize - 1) {
                                        conditionQueryBuilder.append(",");
                                    }
                                }
                            } else {
                                ColumnDefinition autoIncrementDefinition = ((TableDefinition) tableDefinition).autoIncrementDefinition;
                                if (autoIncrementDefinition != null) {
                                    conditionQueryBuilder.appendMockCondition(definition.definitionClassName + "." + autoIncrementDefinition.columnName.toUpperCase(), "\"?\"");
                                }
                            }
                            conditionQueryBuilder.appendEndCreation();
                            javaWriter.emitStatement(conditionQueryBuilder.getQuery());

                        }
                    }, "ConditionQueryBuilder<" + tableDefinition.getModelClassName() + ">", "createFullModelWhere", Sets.newHashSet(Modifier.PUBLIC));

            WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                @Override
                public void write(JavaWriter javaWriter) throws IOException {
                    MockConditionQueryBuilder conditionQueryBuilder = new MockConditionQueryBuilder("return ");
                    conditionQueryBuilder.appendCreation(tableDefinition.getModelClassName());
                    int primaryColumnSize = tableDefinition.getPrimaryColumnDefinitions().size();
                    if (primaryColumnSize > 0) {
                        for (int i = 0; i < definition.primaryColumnDefinitions.size(); i++) {
                            ColumnDefinition columnDefinition = definition.primaryColumnDefinitions.get(i);
                            conditionQueryBuilder.appendMockCondition(definition.definitionClassName + "." + columnDefinition.columnName.toUpperCase(), "\"?\"");

                            if (i < definition.primaryColumnDefinitions.size() - 1) {
                                conditionQueryBuilder.append(",");
                            }
                        }
                    } else {
                        ColumnDefinition autoIncrementDefinition = ((TableDefinition) tableDefinition).autoIncrementDefinition;
                        if (autoIncrementDefinition != null) {
                            conditionQueryBuilder.appendMockCondition(definition.definitionClassName + "." + autoIncrementDefinition.columnName.toUpperCase(), "\"?\"");
                        }
                    }

                    conditionQueryBuilder.appendEndCreation();
                    javaWriter.emitStatement(conditionQueryBuilder.getQuery());
                }
            }, "ConditionQueryBuilder<" + tableDefinition.getModelClassName() + ">", "createPrimaryModelWhere", Sets.newHashSet(Modifier.PUBLIC));
        }
    }
}
