package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.processor.definition.ColumnDefinition;
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

    private TableDefinition tableDefinition;
    private final boolean isModelContainer;

    public WhereQueryWriter(TableDefinition tableDefinition, boolean isModelContainer) {
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
                conditionQueryBuilder.appendCreation(tableDefinition.modelClassName);
                for (int i = 0; i < tableDefinition.primaryColumnDefinitions.size(); i++) {
                    ColumnDefinition columnDefinition = tableDefinition.primaryColumnDefinitions.get(i);
                    conditionQueryBuilder.appendMockCondition(ModelUtils.getStaticMember(tableDefinition.tableSourceClassName, columnDefinition.columnName),
                            ModelUtils.getVariableAccessStatement(columnDefinition.columnFieldName, isModelContainer));
                }
                conditionQueryBuilder.appendEndCreation();
                javaWriter.emitStatement(conditionQueryBuilder.getQuery());

            }
        }, "String", "getPrimaryModelWhere", Sets.newHashSet(Modifier.PUBLIC),
                ModelUtils.getParameter(isModelContainer ,tableDefinition.modelClassName),
                ModelUtils.getVariable(isModelContainer));

        // Don't write empty statement for Model Container
        if(!isModelContainer) {
            javaWriter.emitEmptyLine();
            javaWriter.emitAnnotation(Override.class);
            WriterUtils.emitMethod(javaWriter, new FlowWriter() {
                @Override
                public void write(JavaWriter javaWriter) throws IOException {
                    StringBuilder retStatement = new StringBuilder("return ");
                    for (int i = 0; i < tableDefinition.primaryColumnDefinitions.size(); i++) {
                        ColumnDefinition columnDefinition = tableDefinition.primaryColumnDefinitions.get(i);
                        retStatement.append(tableDefinition.tableSourceClassName + "." + columnDefinition.columnName.toUpperCase())
                                .append(" + \" = ?\"");
                        if (i < tableDefinition.primaryColumnDefinitions.size() - 1) {
                            retStatement.append("+ \"AND\" ");
                        }
                    }

                    javaWriter.emitStatement(retStatement.toString());
                }
            }, "String", "getPrimaryModelWhere", Sets.newHashSet(Modifier.PUBLIC));
        }
    }
}
