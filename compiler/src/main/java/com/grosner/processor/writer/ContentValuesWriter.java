package com.grosner.processor.writer;

import com.google.common.collect.Sets;
import com.grosner.processor.definition.ColumnDefinition;
import com.grosner.processor.definition.TableDefinition;
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
public class ContentValuesWriter implements FlowWriter {

    private TableDefinition tableDefinition;

    private String mSaveMethod;

    private String mSaveVariable;

    private boolean isModelContainer;

    public ContentValuesWriter(TableDefinition tableDefinition, boolean isModelContainer) {
        this.tableDefinition = tableDefinition;
        this.isModelContainer = isModelContainer;
        mSaveMethod = isModelContainer ? "ModelContainerUtils" : "SqlUtils";
        mSaveVariable = isModelContainer ? "modelContainer" : "model";
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("ContentValues contentValues = new ContentValues()");
                for (ColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {
                    columnDefinition.writeSaveDefinition(javaWriter, isModelContainer);
                }
                javaWriter.emitEmptyLine();

                javaWriter.emitStatement(mSaveMethod + ".save(%1s, %1s, %1s, %1s)", "async", mSaveVariable, "contentValues", "saveMode");
            }
        }, "void", "save", Sets.newHashSet(Modifier.PUBLIC), "boolean", "async",
                ModelUtils.getParameter(isModelContainer,tableDefinition.getModelClassName()), mSaveVariable, "int", "saveMode");
    }
}
