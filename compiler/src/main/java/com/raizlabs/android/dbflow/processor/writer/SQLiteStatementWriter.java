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

    private boolean isModelContainerAdapter;

    private final boolean implementsSqlStatementListener;
    private final boolean implementsContentValuesListener;

    public SQLiteStatementWriter(TableDefinition tableDefinition, boolean isModelContainerAdapter,
                                 boolean implementsSqlStatementListener, boolean implementsContentValuesListener) {
        this.tableDefinition = tableDefinition;
        this.isModelContainerAdapter = isModelContainerAdapter;
        this.implementsSqlStatementListener = implementsSqlStatementListener;
        this.implementsContentValuesListener = implementsContentValuesListener;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        final String[] args = new String[4];
        args[0] = Classes.SQLITE_STATEMENT;
        args[1] = "statement";
        args[2] = isModelContainerAdapter ? "ModelContainer<" + tableDefinition.getModelClassName() + ", ?>"
                : tableDefinition.getModelClassName();
        args[3] = ModelUtils.getVariable(isModelContainerAdapter);
        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {

                AtomicInteger columnCounter = new AtomicInteger(1);
                for (int i = 0; i < tableDefinition.getColumnDefinitions().size(); i++) {
                    ColumnDefinition columnDefinition = tableDefinition.getColumnDefinitions().get(i);
                    if(!columnDefinition.isPrimaryKeyAutoIncrement) {
                        columnDefinition.writeSaveDefinition(javaWriter, isModelContainerAdapter, false, columnCounter);
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
        writeContentValues(false, javaWriter, args);
        writeContentValues(true, javaWriter, args);
    }

    private void writeContentValues(final boolean isInsert, JavaWriter javaWriter, final String[] args) {

        WriterUtils.emitOverriddenMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {

                AtomicInteger columnCounter = new AtomicInteger(1);
                for (int i = 0; i < tableDefinition.getColumnDefinitions().size(); i++) {
                    ColumnDefinition columnDefinition = tableDefinition.getColumnDefinitions().get(i);
                    if((isInsert && !columnDefinition.isPrimaryKeyAutoIncrement) || !isInsert) {
                        columnDefinition.writeSaveDefinition(javaWriter, isModelContainerAdapter, true, columnCounter);
                    }
                }
                if(implementsContentValuesListener) {
                    if(!isInsert) {
                        javaWriter.emitStatement("%1s.onBindToContentValues(%1s)", args[3], args[1]);
                    } else {
                        javaWriter.emitStatement("%1s.onBindToInsertValues(%1s)", args[3], args[1]);
                    }
                }
                javaWriter.emitEmptyLine();
            }
        }, "void", isInsert ? "bindToInsertValues" : "bindToContentValues", Sets.newHashSet(Modifier.PUBLIC), args);
    }
}
