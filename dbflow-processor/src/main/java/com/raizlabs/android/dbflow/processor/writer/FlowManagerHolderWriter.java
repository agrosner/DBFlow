package com.raizlabs.android.dbflow.processor.writer;

import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition;
import com.raizlabs.android.dbflow.processor.definition.TypeDefinition;
import com.raizlabs.android.dbflow.processor.handler.DatabaseHandler;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

/**
 * Description: Top-level writer that handles writing all {@link com.raizlabs.android.dbflow.processor.writer.DatabaseWriter}
 * and {@link com.raizlabs.android.dbflow.annotation.TypeConverter}
 */
public class FlowManagerHolderWriter implements TypeDefinition {

    private final ProcessorManager processorManager;

    public FlowManagerHolderWriter(ProcessorManager processorManager) {
        this.processorManager = processorManager;
    }

    @Override
    public TypeSpec getTypeSpec() {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(ClassNames.DATABASE_HOLDER_STATIC_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        for (DatabaseWriter databaseWriter : processorManager.getManagerWriters()) {
            constructor.addStatement("new $T(this)", databaseWriter.elementClassName);
        }
        for (TypeConverterDefinition typeConverterDefinition : processorManager.getTypeConverters()) {
            constructor.addStatement("$L.put(%1s.class, new %1s())", DatabaseHandler.TYPE_CONVERTER_MAP_FIELD_NAME,
                    typeConverterDefinition.getModelTypeName(),
                    typeConverterDefinition.getClassName());
        }


        typeBuilder.addMethod(constructor.build());


        return typeBuilder.build();
    }
}
