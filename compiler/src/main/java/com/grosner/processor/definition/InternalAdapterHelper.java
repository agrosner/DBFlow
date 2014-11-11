package com.grosner.processor.definition;

import com.google.common.collect.Sets;
import com.grosner.processor.utils.ModelUtils;
import com.grosner.processor.utils.WriterUtils;
import com.grosner.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class InternalAdapterHelper {

    public static void writeGetModelClass(JavaWriter javaWriter, final String modelClassName) throws IOException {
        javaWriter.emitEmptyLine()
                .emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return " + ModelUtils.getFieldClass(modelClassName));
            }
        }, "Class<" + modelClassName + ">", "getModelClass", Sets.newHashSet(Modifier.PUBLIC));
    }

    public static void writeGetTableName(JavaWriter javaWriter, final String tableSourceClassName) throws IOException {
        javaWriter
                .emitEmptyLine()
                .emitAnnotation(Override.class);
        WriterUtils.emitMethod(javaWriter, new FlowWriter() {
            @Override
            public void write(JavaWriter javaWriter) throws IOException {
                javaWriter.emitStatement("return " + ModelUtils.getStaticMember(tableSourceClassName, "TABLE_NAME"));
            }
        }, "String", "getTableName", Sets.newHashSet(Modifier.PUBLIC));
    }
}
