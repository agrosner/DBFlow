package com.grosner.processor.model;

import com.google.common.collect.Sets;
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

    public WhereQueryWriter(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }
    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                StringBuilder retStatement = new StringBuilder("return ");
                for (int i = 0; i < tableDefinition.primaryColumnDefinitions.size(); i++) {
                    ColumnDefinition columnDefinition = tableDefinition.primaryColumnDefinitions.get(i);
                    retStatement.append(tableDefinition.tableSourceClassName + "." + columnDefinition.columnName.toUpperCase())
                            .append(" + \" = \" + ").append("model.").append(columnDefinition.columnFieldName);
                    if (i < tableDefinition.primaryColumnDefinitions.size() - 1) {
                        retStatement.append("+ \"AND\" ");
                    }
                }

                javaWriter.emitStatement(retStatement.toString());

            }
        }, "String", "getPrimaryModelWhere", Sets.newHashSet(Modifier.PUBLIC), tableDefinition.modelClassName, "model");
    }
}
