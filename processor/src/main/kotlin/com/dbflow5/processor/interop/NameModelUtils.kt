package com.dbflow5.processor.interop

import com.dbflow5.codegen.model.NameModel
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
