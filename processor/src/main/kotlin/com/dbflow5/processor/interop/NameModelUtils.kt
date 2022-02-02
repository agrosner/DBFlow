package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.processor.utils.getPackage
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement

operator fun NameModel.Companion.invoke(
    simpleName: Name,
    packageElement: PackageElement,
    nullable: Boolean = false,
) = NameModel(
    packageName = packageElement.qualifiedName.toString(),
    shortName = simpleName.toString(),
    nullable = nullable,
)

fun TypeElement.name() = NameModel(
    simpleName,
    getPackage(),
)

fun ExecutableElement.name() = NameModel(
    simpleName,
    getPackage(),
)
