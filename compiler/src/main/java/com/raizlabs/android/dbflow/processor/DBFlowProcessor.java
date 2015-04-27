package com.raizlabs.android.dbflow.processor;

import com.google.auto.service.AutoService;
import com.raizlabs.android.dbflow.annotation.*;
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;
import com.raizlabs.android.dbflow.processor.handler.*;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Author: andrewgrosner
 */
@AutoService(Processor.class)
public class DBFlowProcessor extends AbstractProcessor {

    public static String DEFAULT_DB_NAME;

    private ProcessorManager manager;

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
        Set<String> supportedTypes = new LinkedHashSet<>();
        supportedTypes.add(Table.class.getName());
        supportedTypes.add(Column.class.getName());
        supportedTypes.add(TypeConverter.class.getName());
        supportedTypes.add(ContainerAdapter.class.getName());
        supportedTypes.add(ModelView.class.getName());
        supportedTypes.add(Migration.class.getName());
        supportedTypes.add(ContentProvider.class.getName());
        supportedTypes.add(TableEndpoint.class.getName());
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

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        manager = new ProcessorManager(processingEnv);
        manager.addHandlers(
                new MigrationHandler(),
                new TypeConverterHandler(),
                new DatabaseHandler(),
                new TableHandler(),
                new ModelContainerHandler(),
                new ModelViewHandler(),
                new ContentProviderHandler(),
                new TableEndpointHandler());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Database.class);
        for(Element element: elements) {
            Database database = element.getAnnotation(Database.class);
            if(database != null) {
                DEFAULT_DB_NAME = database.name();
                break;
            }
        }
        manager.handle(manager, roundEnv);

        // return true if we successfully processed the Annotation.
        return true;
    }

}
