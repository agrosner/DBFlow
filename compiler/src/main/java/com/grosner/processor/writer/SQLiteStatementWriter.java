package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.dbflow.annotation.Column;
import com.grosner.processor.Classes;
import com.grosner.processor.definition.ColumnDefinition;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class SQLiteStatementWriter implements FlowWriter {

    private TableDefinition tableDefinition;

    private boolean isModelContainer;

    public SQLiteStatementWriter(TableDefinition tableDefinition, boolean isModelContainer) {
        this.tableDefinition = tableDefinition;
        this.isModelContainer = isModelContainer;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        String[] args = new String[4];
        args[0] = Classes.SQLITE_STATEMENT;
        args[1] = "statement";
        args[2] = isModelContainer ? "ModelContainer<" + tableDefinition.getModelClassName() + ", ?>"
                : tableDefinition.getModelClassName();
        args[3] = ModelUtils.getVariable(isModelContainer);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {

                AtomicInteger columnCounter = new AtomicInteger(1);
                for (int i = 0; i < tableDefinition.getColumnDefinitions().size(); i++) {
                    ColumnDefinition columnDefinition = tableDefinition.getColumnDefinitions().get(i);
                    if(columnDefinition.columnType != Column.PRIMARY_KEY_AUTO_INCREMENT) {
                        columnDefinition.writeSaveDefinition(javaWriter, isModelContainer,columnCounter);
                    }
                }
                javaWriter.emitEmptyLine();
            }
        }, "void", "bindToStatement", Sets.newHashSet(Modifier.PUBLIC), args);
    }
}
