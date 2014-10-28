package com.grosner.processor.model;

import com.google.common.collect.Sets;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class LoadCursorWriter implements FlowWriter {

    private TableDefinition tableDefinition;

    public LoadCursorWriter(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    @Override
    public String getFQCN() {
        return null;
    }

    @Override
    public void write(JavaWriter javaWriter) throws IOException {
        javaWriter.emitEmptyLine();
        javaWriter.emitAnnotation(Override.class);
        javaWriter.beginMethod("void", "loadFromCursor", Sets.newHashSet(Modifier.PUBLIC), "Cursor", "cursor");
        javaWriter.endMethod();
    }
}
