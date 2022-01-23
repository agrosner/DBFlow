package com.dbflow5.ksp

import com.dbflow5.codegen.shared.Annotations
import com.dbflow5.codegen.shared.parser.validation.ValidationException
import com.dbflow5.ksp.model.interop.KSPResolver
import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.grosner.dbflow5.codegen.kotlin.writer.ObjectWriter
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Description:
 */
class DBFlowKspProcessor(
    private val ksClassDeclarationParser: KSClassDeclarationParser,
    private val environment: SymbolProcessorEnvironment,
    private val objectWriter: ObjectWriter,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        try {
            ksClassDeclarationParser.applyResolver(resolver)
            val symbols =
                Annotations.values.map {
                    resolver.getSymbolsWithAnnotation(
                        it.qualifiedName
                    ).toList()
                }.flatten().distinct()
            if (symbols.isNotEmpty()) {
                val objects = symbols.mapNotNull { annotated ->
                    when (annotated) {
                        is KSClassDeclaration -> {
                            ksClassDeclarationParser.parse(annotated)
                        }
                        else -> null
                    }
                }.flatten()
                objectWriter.write(
                    KSPResolver(resolver),
                    objects
                ) {
                    it.writeTo(
                        environment.codeGenerator,
                        aggregating = false,
                    )
                }
            }
        } catch (exception: ValidationException) {
            environment.logger.exception(exception)
        }

        return listOf()
    }
}