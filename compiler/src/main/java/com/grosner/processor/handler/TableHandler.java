package com.grosner.processor.handler;

import com.grosner.dbflow.annotation.Table;
import com.grosner.processor.definition.TableDefinition;
import com.grosner.processor.model.ProcessorManager;
import com.squareup.javawriter.JavaWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class TableHandler {

    public TableHandler(RoundEnvironment roundEnv, ProcessorManager manager) {

        ProcessingEnvironment processingEnv = manager.getProcessingEnvironment();

        final Set<? extends Element> annotatedElements = roundEnv
                .getElementsAnnotatedWith(Table.class);

        //process() gets called more than once, annotatedElements might be empty an empty Set in one of those calls( i.e. when there are no annotations to process this round).
        if (annotatedElements.size() > 0) {

            Iterator<? extends Element> iterator = annotatedElements.iterator();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                System.out.println(element.asType());

                try {
                    final String packageName = processingEnv.getElementUtils()
                            .getPackageOf(element).toString();
                    TableDefinition tableDefinition = new TableDefinition(processingEnv, packageName, element);
                    JavaWriter javaWriter = new JavaWriter(processingEnv.getFiler().createSourceFile(tableDefinition.getFQCN()).openWriter());
                    tableDefinition.write(javaWriter);
                    javaWriter.close();

                    tableDefinition.writeAdapter(processingEnv);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
