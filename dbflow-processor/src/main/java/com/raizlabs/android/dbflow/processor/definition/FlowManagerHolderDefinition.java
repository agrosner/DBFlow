package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.handler.DatabaseHandler;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

/**
 * Description: Top-level writer that handles writing all {@link DatabaseDefinition}
 * and {@link com.raizlabs.android.dbflow.annotation.TypeConverter}
 */
public class FlowManagerHolderDefinition implements TypeDefinition {

    private final ProcessorManager processorManager;

    public FlowManagerHolderDefinition(ProcessorManager processorManager) {
        this.processorManager = processorManager;
    }

    @Override
    public TypeSpec getTypeSpec() {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(ClassNames.DATABASE_HOLDER_STATIC_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ClassNames.DATABASE_HOLDER);

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        for (DatabaseDefinition databaseDefinition : processorManager.getDatabaseDefinitionMap()) {
            constructor.addStatement("new $T(this)", databaseDefinition.outputClassName);
        }
        for (TypeConverterDefinition typeConverterDefinition : processorManager.getTypeConverters()) {
            constructor.addStatement("$L.put($T.class, new $T())", DatabaseHandler.TYPE_CONVERTER_MAP_FIELD_NAME,
                    typeConverterDefinition.getModelTypeName(),
                    typeConverterDefinition.getClassName());
        }


        typeBuilder.addMethod(constructor.build());


        return typeBuilder.build();
    }
}
