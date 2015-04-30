package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.model.builder.MockConditionQueryBuilder;
import com.raizlabs.android.dbflow.processor.model.writer.ColumnAccessModel;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Description: Writes the completed SQL statement that contains its primary key values checking for existence.
 */
public class WhereQueryWriter implements FlowWriter {

    private BaseTableDefinition tableDefinition;
    private final boolean isModelContainerAdapter;

    public WhereQueryWriter(BaseTableDefinition tableDefinition, boolean isModelContainerAdapter) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
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
                                ColumnAccessModel accessModel = new ColumnAccessModel(tableDefinition.getManager(), columnDefinition,
                                                                                      isModelContainerAdapter);
                                conditionQueryBuilder.appendMockCondition(ModelUtils.getStaticMember(tableDefinition.getTableSourceClassName(), columnDefinition.columnName),
                                        accessModel.getQueryNoCast());
                                if (i < tableDefinition.getPrimaryColumnDefinitions().size() - 1) {
                                    conditionQueryBuilder.append(",");
                                }
                            }
                        } else if (!(tableDefinition instanceof ModelViewDefinition)) {
                            ColumnDefinition autoIncrementDefinition = ((TableDefinition) tableDefinition).autoIncrementDefinition;
                            ColumnAccessModel accessModel = new ColumnAccessModel(tableDefinition.getManager(), autoIncrementDefinition,
                                                                                  isModelContainerAdapter);
                            if (autoIncrementDefinition != null) {
                                conditionQueryBuilder
                                        .appendMockCondition(ModelUtils.getStaticMember(tableDefinition.getTableSourceClassName(),
                                                                                        autoIncrementDefinition.columnName),
                                                                          accessModel.getQueryNoCast());
                            }
                        }
                        conditionQueryBuilder.appendEndCreation();
                        javaWriter.emitStatement(conditionQueryBuilder.getQuery());

                    }
                }, "ConditionQueryBuilder<" + tableDefinition.getModelClassName() + ">", "getPrimaryModelWhere", Sets.newHashSet(Modifier.PUBLIC),
                ModelUtils.getParameter(isModelContainerAdapter, tableDefinition.getModelClassName()),
                ModelUtils.getVariable(isModelContainerAdapter));

        // Don't write empty statement for Model Container
        if (!isModelContainerAdapter && !(tableDefinition instanceof ModelViewDefinition)) {
            final TableDefinition definition = ((TableDefinition) tableDefinition);
            WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                @Override
                public void write(JavaWriter javaWriter) throws IOException {
                    MockConditionQueryBuilder conditionQueryBuilder = new MockConditionQueryBuilder("return ");
                    conditionQueryBuilder.appendCreation(tableDefinition.getModelClassName());
                    int primaryColumnSize = tableDefinition.getPrimaryColumnDefinitions().size();
                    List<String> primaryColumnNames = new ArrayList<>();
                    if (primaryColumnSize > 0) {
                        for (int i = 0; i < definition.primaryColumnDefinitions.size(); i++) {
                            ColumnDefinition columnDefinition = definition.primaryColumnDefinitions.get(i);
                            primaryColumnNames.add(definition.definitionClassName + "." + columnDefinition.columnName.toUpperCase());
                        }
                    } else {
                        ColumnDefinition autoIncrementDefinition = ((TableDefinition) tableDefinition).autoIncrementDefinition;
                        if (autoIncrementDefinition != null) {
                            primaryColumnNames.add(definition.definitionClassName + "." + autoIncrementDefinition.columnName.toUpperCase());
                        }
                    }
                    conditionQueryBuilder.appendEmptyMockConditions(primaryColumnNames);
                    conditionQueryBuilder.appendEndCreation();
                    javaWriter.emitStatement(conditionQueryBuilder.getQuery());
                }
            }, "ConditionQueryBuilder<" + tableDefinition.getModelClassName() + ">", "createPrimaryModelWhere", Sets.newHashSet(Modifier.PUBLIC));
        }
    }
}
