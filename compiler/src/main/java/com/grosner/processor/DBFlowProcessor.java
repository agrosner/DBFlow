package com.grosner.processor;

import com.google.auto.service.AutoService;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.grosner.processor.model.TableDefinition;
import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@AutoService(Processor.class)
public class DBFlowProcessor extends AbstractProcessor {

    /**
     * If the processor class is annotated with {@link
     * javax.annotation.processing.SupportedAnnotationTypes}, return an unmodifiable set with the
     * same set of strings as the annotation.  If the class is not so
     * annotated, an empty set is returned.
     *
     * @return the names of the annotation types supported by this
     * processor, or an empty set if none
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedTypes = new LinkedHashSet<String>();
        supportedTypes.add(Table.class.getName());
        supportedTypes.add(Column.class.getName());
        return supportedTypes;
    }

    /**
     * If the processor class is annotated with {@link
     * javax.annotation.processing.SupportedSourceVersion}, return the source version in the
     * annotation.  If the class is not so annotated, {@link
     * javax.lang.model.SourceVersion#RELEASE_6} is returned.
     *
     * @return the latest source version supported by this processor
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * {@inheritDoc}
     *
     * @param annotations
     * @param roundEnv
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<? extends Element> annotatedElements = roundEnv
                .getElementsAnnotatedWith(Table.class);

        //process() gets called more than once, annotatedElements might be empty an empty Set in one of those calls( i.e. when there are no annotations to process this round).
        if (annotatedElements.size() > 0) {

            Iterator<? extends Element> iterator = annotatedElements.iterator();
            while(iterator.hasNext()) {
                Element element = iterator.next();
                System.out.println(element.asType());

                try {
                    final String packageName = processingEnv.getElementUtils()
                            .getPackageOf(element).toString();
                    TableDefinition tableDefinition = new TableDefinition(packageName, element);
                    JavaWriter javaWriter = new JavaWriter(processingEnv.getFiler().createSourceFile(tableDefinition.getFQCN()).openWriter());
                    tableDefinition.write(javaWriter);
                    javaWriter.close();

                    tableDefinition.writeAdapter(processingEnv);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        // return true if we successfully processed the Annotation.
        return true;
    }

    private void createHeader(JavaWriter javaWriter, String packageName) throws IOException {
        javaWriter.emitPackage(packageName);
    }
}
