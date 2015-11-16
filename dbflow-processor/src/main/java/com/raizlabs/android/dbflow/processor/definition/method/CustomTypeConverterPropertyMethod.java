package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition;
import com.raizlabs.android.dbflow.processor.definition.CodeAdder;
import com.raizlabs.android.dbflow.processor.definition.TypeAdder;
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Description: Writes out the custom type converter fields.
 */
public class CustomTypeConverterPropertyMethod implements TypeAdder, CodeAdder {

    private final BaseTableDefinition baseTableDefinition;

    public CustomTypeConverterPropertyMethod(BaseTableDefinition baseTableDefinition) {
        this.baseTableDefinition = baseTableDefinition;
    }


    @Override
    public void addToType(TypeSpec.Builder typeBuilder) {
        Set<ClassName> customTypeConverters = baseTableDefinition.getAssociatedTypeConverters().keySet();
        for (ClassName className : customTypeConverters) {
            typeBuilder.addField(FieldSpec.builder(className, "typeConverter" + className.simpleName(), Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("new $T()", className)
                    .build());
        }

        Set<ClassName> globalTypeConverters = baseTableDefinition.getGlobalTypeConverters().keySet();
        for (ClassName className : globalTypeConverters) {
            typeBuilder.addField(FieldSpec.builder(className, "global_typeConverter" + className.simpleName(), Modifier.PRIVATE, Modifier.FINAL)
                    .build());
        }


    }

    @Override
    public void addCode(CodeBlock.Builder code) {
        // Constructor code
        Set<ClassName> globalTypeConverters = baseTableDefinition.getGlobalTypeConverters().keySet();
        for (ClassName className : globalTypeConverters) {
            List<ColumnDefinition> def = baseTableDefinition.getGlobalTypeConverters().get(className);
            TypeName firstTypeName = def.get(0).elementTypeName;
            code.addStatement("global_typeConverter$L = ($T) $L.getTypeConverterForClass($T.class)",
                    className.simpleName(), className,
                    "holder", firstTypeName)
                    .build();
        }
    }
}
