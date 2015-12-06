package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.handler.DatabaseHandler;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;

import javax.lang.model.element.Modifier;

/**
 * Description: Top-level writer that handles writing all {@link DatabaseDefinition}
 * and {@link com.raizlabs.android.dbflow.annotation.TypeConverter}
 */
public class FlowManagerHolderDefinition implements TypeDefinition {
    private final ProcessorManager processorManager;

    private String className = "";

    private static final String OPTION_TARGET_MODULE_NAME = "targetModuleName";

    public FlowManagerHolderDefinition(ProcessorManager processorManager) {
        this.processorManager = processorManager;

        Map<String, String> options = this.processorManager.getProcessingEnvironment().getOptions();

        if (options.containsKey(OPTION_TARGET_MODULE_NAME)) {
            className = options.get(OPTION_TARGET_MODULE_NAME);
        }

        className += ClassNames.DATABASE_HOLDER_STATIC_CLASS_NAME;
    }

    @Override
    public TypeSpec getTypeSpec() {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(this.className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .superclass(ClassNames.DATABASE_HOLDER);

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC);

        for (TypeConverterDefinition typeConverterDefinition : processorManager.getTypeConverters()) {
            constructor.addStatement("$L.put($T.class, new $T())", DatabaseHandler.TYPE_CONVERTER_MAP_FIELD_NAME,
                typeConverterDefinition.getModelTypeName(),
                typeConverterDefinition.getClassName());
        }

        for (DatabaseDefinition databaseDefinition : processorManager.getDatabaseDefinitionMap()) {
            constructor.addStatement("new $T(this)", databaseDefinition.outputClassName);
        }

        typeBuilder.addMethod(constructor.build());


        return typeBuilder.build();
    }
}