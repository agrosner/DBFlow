package com.dbflow5.processor.definition

import com.dbflow5.annotation.ColumnMap
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.definition.column.ReferenceColumnDefinition
import com.dbflow5.processor.utils.ElementUtility
import com.dbflow5.processor.utils.annotation
import javax.lang.model.element.Element

/**
 * Description: Generates a [ColumnDefinition] or [ReferenceColumnDefinition] based on the [Element]
 * passed into the [generate] method.
 */
class BasicColumnGenerator(private val manager: ProcessorManager) {

    /**
     * Generates a [ColumnDefinition]. If null, there is a field that is package private not in the same package that we
     * did not generate code for to access.
     */
    fun generate(element: Element, entityDefinition: EntityDefinition): ColumnDefinition? {
        val isColumnMap = element.annotation<ColumnMap>() != null
        val isPackagePrivate = ElementUtility.isPackagePrivate(element)
        val isPackagePrivateNotInSamePackage = isPackagePrivate && !ElementUtility.isInSamePackage(manager, element, entityDefinition.element)

        if (checkInheritancePackagePrivate(isPackagePrivateNotInSamePackage, element)) return null

        return if (isColumnMap) {
            ReferenceColumnDefinition(element.annotation<ColumnMap>()!!, manager, entityDefinition, element, isPackagePrivateNotInSamePackage)
        } else {
            ColumnDefinition(manager, element, entityDefinition, isPackagePrivateNotInSamePackage)
        }
    }

    /**
     * Do not support inheritance on package private fields without having ability to generate code for it in
     * same package.
     */
    private fun checkInheritancePackagePrivate(isPackagePrivateNotInSamePackage: Boolean, element: Element): Boolean {
        if (isPackagePrivateNotInSamePackage && !manager.elementBelongsInTable(element)) {
            manager.logError("Package private inheritance on non-table/querymodel/view " +
                "is not supported without a @InheritedColumn annotation." +
                " Make $element from ${element.enclosingElement} public or private.")
            return true
        }
        return false
    }
}