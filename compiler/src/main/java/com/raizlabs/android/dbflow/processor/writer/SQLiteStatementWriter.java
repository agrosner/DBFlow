package com.raizlabs.android.dbflow.processor.writer;

import com.google.common.collect.Sets;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.processor.Classes;
import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.raizlabs.android.dbflow.processor.utils.WriterUtils;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: Writes the bindTo contentValues and SQLiteStatement methods in a ModelAdapter and ContainerAdapter.
 */
public class SQLiteStatementWriter implements FlowWriter {

    private TableDefinition tableDefinition;

    private boolean isModelContainer;

    private final boolean implementsSqlStatementListener;
    private final boolean implementsContentValuesListener;

    public SQLiteStatementWriter(TableDefinition tableDefinition, boolean isModelContainer,
                                 boolean implementsSqlStatementListener, boolean implementsContentValuesListener) {
        this.tableDefinition = tableDefinition;
        this.isModelContainer = isModelContainer;
        this.implementsSqlStatementListener = implementsSqlStatementListener;
        this.implementsContentValuesListener = implementsContentValuesListener;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        final String[] args = new String[4];
        args[0] = Classes.SQLITE_STATEMENT;
        args[1] = "statement";
        args[2] = isModelContainer ? "ModelContainer<" + tableDefinition.getModelClassName() + ", ?>"
                : tableDefinition.getModelClassName();
        args[3] = ModelUtils.getVariable(isModelContainer);
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {

                AtomicInteger columnCounter = new AtomicInteger(1);
                for (int i = 0; i < tableDefinition.getColumnDefinitions().size(); i++) {
                    ColumnDefinition columnDefinition = tableDefinition.getColumnDefinitions().get(i);
                    if(columnDefinition.columnType != Column.PRIMARY_KEY_AUTO_INCREMENT) {
                        columnDefinition.writeSaveDefinition(javaWriter, isModelContainer, false, columnCounter);
                    }

                    if(implementsSqlStatementListener) {
                        javaWriter.emitStatement("%1s.onBindToStatement(%1s)", args[3], args[1]);
                    }
                }
                javaWriter.emitEmptyLine();
            }
        }, "void", "bindToStatement", Sets.newHashSet(Modifier.PUBLIC), args);

        args[0] = Classes.CONTENT_VALUES;
        args[1] = "contentValues";
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {

                AtomicInteger columnCounter = new AtomicInteger(1);
                for (int i = 0; i < tableDefinition.getColumnDefinitions().size(); i++) {
                    ColumnDefinition columnDefinition = tableDefinition.getColumnDefinitions().get(i);
                    if(columnDefinition.columnType != Column.PRIMARY_KEY_AUTO_INCREMENT) {
                        columnDefinition.writeSaveDefinition(javaWriter, isModelContainer, true, columnCounter);
                    }

                    if(implementsContentValuesListener) {
                        javaWriter.emitStatement("%1s.onBindToContentValues(%1s)", args[3], args[1]);
                    }
                }
                javaWriter.emitEmptyLine();
            }
        }, "void", "bindToContentValues", Sets.newHashSet(Modifier.PUBLIC), args);
    }
}
