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
import java.util.ArrayList;
import java.util.List;

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

        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
                    @Override
                    public void write(JavaWriter javaWriter) throws IOException {
                        MockConditionQueryBuilder conditionQueryBuilder = new MockConditionQueryBuilder("ConditionQueryBuilder<")
                                .append(tableDefinition.getModelClassName()).append("> query = ")
                                .appendEmptyCreation(tableDefinition.getModelClassName()).appendEndCreation();
                        javaWriter.emitStatement(conditionQueryBuilder.getQuery());

                        int columnSize = tableDefinition.getColumnDefinitions().size();
                        if (columnSize > 0) {
                            for (int i = 0; i < columnSize; i++) {
                                ColumnDefinition columnDefinition = tableDefinition.getColumnDefinitions().get(i);
                                if(columnDefinition.columnType == Column.FOREIGN_KEY) {
                                    String fieldAccess = columnDefinition.columnFieldName;
                                    if(isModelContainer) {
                                        fieldAccess = String.format("getValue(\"%1s\")", fieldAccess);
                                    }
                                    javaWriter.beginControlFlow("if (%1s.%1s != null)", ModelUtils.getVariable(isModelContainer), fieldAccess);
                                    for(ForeignKeyReference reference: columnDefinition.foreignKeyReferences) {
                                        String access = ModelUtils.getAccessStatement(columnDefinition.columnName, ModelUtils.getClassFromAnnotation(reference),
                                                reference.foreignColumnName(), reference.foreignColumnName(), columnDefinition.isModelContainer,isModelContainer, true, false);
                                        String condition = new MockConditionQueryBuilder("query").appendMockContinueCondition(ModelUtils.getStaticMember(tableDefinition.getTableSourceClassName(), columnDefinition.getReferenceColumnName(reference)),
                                                access).getQuery();
                                        javaWriter.emitStatement(condition);
                                    }
                                    javaWriter.endControlFlow();
                                } else {
                                    String condition = new MockConditionQueryBuilder("query").appendMockContinueCondition(ModelUtils.getStaticMember(tableDefinition.getTableSourceClassName(), columnDefinition.columnName),
                                            ModelUtils.getAccessStatement(columnDefinition.columnName, columnDefinition.columnFieldType,
                                                    columnDefinition.columnFieldName, columnDefinition.containerKeyName, isModelContainer, false, false, columnDefinition.hasTypeConverter)).getQuery();
                                    javaWriter.emitStatement(condition);
                                }
                            }
                        }

                        javaWriter.emitStatement("return query");

                    }
                }, "ConditionQueryBuilder<" + tableDefinition.getModelClassName() + ">", "getFullModelWhere", Sets.newHashSet(Modifier.PUBLIC),
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
                    List<String> columnNames = new ArrayList<>();
                    if (columnSize > 0) {
                        for (int i = 0; i < columnSize; i++) {
                            ColumnDefinition columnDefinition = definition.getColumnDefinitions().get(i);
                            if (columnDefinition.columnType == Column.FOREIGN_KEY) {
                                for (ForeignKeyReference reference : columnDefinition.foreignKeyReferences) {
                                    columnNames.add(definition.definitionClassName + "." + columnDefinition.getReferenceColumnName(reference));
                                }
                            } else {
                                columnNames.add(definition.definitionClassName + "." + columnDefinition.columnName.toUpperCase());
                            }
                        }
                    }
                    conditionQueryBuilder.appendEmptyMockConditions(columnNames);
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
