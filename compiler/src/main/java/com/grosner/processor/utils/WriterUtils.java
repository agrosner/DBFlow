package com.grosner.processor.utils;

import com.grosner.processor.writer.FlowWriter;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class WriterUtils {

    public static void emitMethod(JavaWriter javaWriter, FlowWriter flowWriter, String returnType, String name, Set<Modifier> modifiers, String...params) {
        try {
            javaWriter.beginMethod(returnType, name, modifiers, params);
            flowWriter.write(javaWriter);
            javaWriter.endMethod();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void emitOverriddenMethod(JavaWriter javaWriter, FlowWriter flowWriter, String returnType, String name, Set<Modifier> modifiers, String...params) {
        try {
            javaWriter.emitEmptyLine().emitAnnotation(Override.class);
            emitMethod(javaWriter, flowWriter, returnType, name, modifiers, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void emitTransactionManagerCall(JavaWriter javaWriter, String method, String variable) throws IOException {
        javaWriter.emitStatement("TransactionManager.getInstance().%1s(ProcessModelInfo.withModels(%1s).info(DBTransactionInfo.create()));",
                method, variable);
    }
}
