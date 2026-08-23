package com.dbflow5.compiler.ir

import com.dbflow5.codegen.shared.validation.ValidationException
import com.dbflow5.compiler.CompilerModule
import com.grosner.dbflow5.codegen.kotlin.writer.ObjectWriter
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.util.isFileClass
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import java.io.File

internal class DBFlowIrGenerationExtension(
    private val generatedDir: String?,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        CreateDbIrTransformer(pluginContext).transform(moduleFragment)
        CompanionPropertyIrTransformer(pluginContext).transform(moduleFragment)
        val output = File(
            generatedDir ?: System.getProperty(GENERATED_DIR_PROPERTY).orEmpty().ifEmpty { return }
        )
        if (moduleFragment.compilesGeneratedSources(output)) return
        output.mkdirs()

        val classes = mutableListOf<IrClass>()
        fun collect(declaration: IrDeclaration) {
            if (declaration is IrClass) {
                if (!declaration.isFileClass && declaration.hasDbFlowAnnotation()) {
                    classes += declaration
                }
                declaration.declarations.forEach(::collect)
            }
        }
        moduleFragment.files.forEach { file ->
            file.declarations.forEach(::collect)
        }
        if (classes.isEmpty()) return

        val known = classes.associateBy { irClass -> irClass.toPoetClassName() }
        val resolver = IrClassNameResolver(pluginContext, known)
        val koinApp = koinApplication {
            modules(CompilerModule.modules())
        }
        val koin: Koin = koinApp.koin
        try {
            val parser = koin.get<IrClassParser>()
            val writer = koin.get<ObjectWriter>()
            val fieldSanitizer = koin.get<com.dbflow5.codegen.shared.parser.FieldSanitizer>()
            fieldSanitizer.applyResolver(resolver)
            val models = classes.flatMap { parser.parse(it) }
            if (models.isEmpty()) return
            writer.write(resolver, models) { fileSpec ->
                fileSpec.writeTo(output)
            }
        } catch (exception: ValidationException) {
            pluginContext.messageCollector.report(
                CompilerMessageSeverity.ERROR,
                exception.message ?: exception.toString(),
            )
        } finally {
            koinApp.close()
        }
    }
}

private fun IrClass.hasDbFlowAnnotation(): Boolean =
    hasAnnotation(IrTypeFqNames.Table) ||
        hasAnnotation(IrTypeFqNames.Query) ||
        hasAnnotation(IrTypeFqNames.ModelView) ||
        hasAnnotation(IrTypeFqNames.Database) ||
        hasAnnotation(IrTypeFqNames.TypeConverter) ||
        hasAnnotation(IrTypeFqNames.ManyToMany) ||
        hasAnnotation(IrTypeFqNames.MultipleManyToMany) ||
        hasAnnotation(IrTypeFqNames.OneToMany) ||
        hasAnnotation(IrTypeFqNames.Migration)

internal const val GENERATED_DIR_PROPERTY = "dbflow.generated.dir"

private fun IrModuleFragment.compilesGeneratedSources(output: File): Boolean {
    val root = output.canonicalFile.toPath()
    return files.any { file ->
        val path = runCatching { File(file.fileEntry.name).canonicalFile.toPath() }.getOrNull()
        path != null && path.startsWith(root)
    }
}
