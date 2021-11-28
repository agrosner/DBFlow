package com.dbflow5.ksp

import com.dbflow5.ksp.parser.KSClassDeclarationParser
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Description:
 */
class DBFlowKspProcessor(
    private val ksClassDeclarationParser: KSClassDeclarationParser,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(
            TableAnnotation.annotationClassName
        ).toList()
        val objects = symbols.mapNotNull { annotated ->
            when (annotated) {
                is KSClassDeclaration -> {
                    ksClassDeclarationParser.parse(annotated)
                }
                else -> null
            }
        }

        println("Objects $objects")

        return listOf()
    }
}