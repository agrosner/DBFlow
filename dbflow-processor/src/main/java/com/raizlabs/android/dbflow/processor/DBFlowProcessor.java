package com.raizlabs.android.dbflow.processor;

import com.google.auto.service.AutoService;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.TypeConverter;
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.annotation.provider.TableEndpoint;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Author: andrewgrosner
 */
@AutoService(Processor.class)
public class DBFlowProcessor extends AbstractProcessor {

    private ProcessorManager manager;

    @Override
    public Set<String> getSupportedOptions() {
        Set <String> supportedOptions = new LinkedHashSet<>();
        supportedOptions.add("targetModuleName");

        return supportedOptions;
    }

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
        supportedTypes.add(Table.class.getCanonicalName());
        supportedTypes.add(Column.class.getCanonicalName());
        supportedTypes.add(TypeConverter.class.getCanonicalName());
        supportedTypes.add(ModelView.class.getCanonicalName());
        supportedTypes.add(Migration.class.getCanonicalName());
        supportedTypes.add(ContentProvider.class.getCanonicalName());
        supportedTypes.add(TableEndpoint.class.getCanonicalName());
        supportedTypes.add(ColumnIgnore.class.getCanonicalName());
        supportedTypes.add(QueryModel.class.getCanonicalName());
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
                new QueryModelHandler(),
                new ModelViewHandler(),
                new ContentProviderHandler(),
                new TableEndpointHandler());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        manager.handle(manager, roundEnv);

        // return true if we successfully processed the Annotation.
        return true;
    }

}
