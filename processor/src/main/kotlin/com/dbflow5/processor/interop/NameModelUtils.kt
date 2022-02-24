package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.processor.utils.getPackage
import com.dbflow5.processor.utils.isNullable
import javax.lang.model.element.Element
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement

operator fun NameModel.Companion.invoke(
    simpleName: Name,
    packageElement: PackageElement,
    nullable: Boolean = false,
) = NameModel(
    packageName = packageElement.qualifiedName.toString(),
    shortName = simpleName.toString(),
    nullable = nullable,
)

fun Element.name(preserveNull: Boolean = false) = NameModel(
    simpleName,
    getPackage(),
    if (preserveNull) isNullable() else false,
)
