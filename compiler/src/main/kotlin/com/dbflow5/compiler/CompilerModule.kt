package com.dbflow5.compiler

import com.dbflow5.codegen.shared.Platforms
import com.dbflow5.codegen.shared.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.shared.parser.FieldSanitizer
import com.dbflow5.codegen.shared.sharedModule
import com.dbflow5.compiler.ir.CompilerPlatforms
import com.dbflow5.compiler.ir.IrClassParser
import com.dbflow5.compiler.ir.IrFieldSanitizer
import com.dbflow5.compiler.ir.IrOriginatingFileTypeSpecAdder
import com.dbflow5.compiler.ir.IrPropertyParser
import com.grosner.dbflow5.codegen.kotlin.codeGenModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal object CompilerModule {
    fun modules(): List<Module> = listOf(
        sharedModule,
        codeGenModule,
        compilerModule,
    )
}

private val compilerModule = module {
    single<Platforms> { CompilerPlatforms }
    single<OriginatingFileTypeSpecAdder> { IrOriginatingFileTypeSpecAdder }
    single { IrPropertyParser() }
    single<FieldSanitizer> { IrFieldSanitizer(get(), get()) }
    single { IrClassParser(get()) }
}
