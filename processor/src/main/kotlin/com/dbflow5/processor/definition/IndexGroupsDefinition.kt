package com.dbflow5.processor.definition

import com.grosner.kpoet.S
import com.grosner.kpoet.`=`
import com.grosner.kpoet.field
import com.grosner.kpoet.final
import com.grosner.kpoet.public
import com.grosner.kpoet.static
import com.dbflow5.annotation.IndexGroup
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.squareup.javapoet.ParameterizedTypeName
import java.util.concurrent.atomic.AtomicInteger

/**
 * Description:
 */
class IndexGroupsDefinition(private val tableDefinition: TableDefinition, indexGroup: IndexGroup) {

    val indexName = indexGroup.name
    val indexNumber = indexGroup.number
    val isUnique = indexGroup.unique

    val columnDefinitionList: MutableList<ColumnDefinition> = arrayListOf()

    val fieldSpec
        get() = field(ParameterizedTypeName.get(com.dbflow5.processor.ClassNames.INDEX_PROPERTY, tableDefinition.elementClassName),
                "index_$indexName") {
            addModifiers(public, static, final)
            `=` {
                add("new \$T<>(${indexName.S}, $isUnique, \$T.class",
                        com.dbflow5.processor.ClassNames.INDEX_PROPERTY, tableDefinition.elementTypeName)

                if (columnDefinitionList.isNotEmpty()) {
                    add(",")
                }
                val index = AtomicInteger(0)
                columnDefinitionList.forEach { it.appendIndexInitializer(this, index) }
                add(")")
            }
        }.build()!!

}
