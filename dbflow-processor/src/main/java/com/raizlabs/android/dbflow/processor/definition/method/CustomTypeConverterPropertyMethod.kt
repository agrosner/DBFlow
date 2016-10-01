package com.raizlabs.android.dbflow.processor.definition.method

import com.raizlabs.android.dbflow.processor.definition.BaseTableDefinition
import com.raizlabs.android.dbflow.processor.definition.CodeAdder
import com.raizlabs.android.dbflow.processor.definition.TypeAdder
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 * Description: Writes out the custom type converter fields.
 */
class CustomTypeConverterPropertyMethod(private val baseTableDefinition: BaseTableDefinition)
: TypeAdder, CodeAdder {


    override fun addToType(typeBuilder: TypeSpec.Builder) {
        val customTypeConverters = baseTableDefinition.associatedTypeConverters.keys
        for (className in customTypeConverters) {
            typeBuilder.addField(FieldSpec.builder(className, "typeConverter" + className.simpleName(),
                    Modifier.PRIVATE, Modifier.FINAL).initializer("new \$T()", className).build())
        }

        val globalTypeConverters = baseTableDefinition.globalTypeConverters.keys
        for (className in globalTypeConverters) {
            typeBuilder.addField(FieldSpec.builder(className, "global_typeConverter" + className.simpleName(),
                    Modifier.PRIVATE, Modifier.FINAL).build())
        }


    }

    override fun addCode(code: CodeBlock.Builder) {
        // Constructor code
        val globalTypeConverters = baseTableDefinition.globalTypeConverters.keys
        for (className in globalTypeConverters) {
            val def = baseTableDefinition.globalTypeConverters[className]
            val firstTypeName = def?.get(0)?.elementTypeName
            code.addStatement("global_typeConverter\$L = (\$T) \$L.getTypeConverterForClass(\$T.class)",
                    className.simpleName(), className,
                    "holder", firstTypeName).build()
        }
    }
}
