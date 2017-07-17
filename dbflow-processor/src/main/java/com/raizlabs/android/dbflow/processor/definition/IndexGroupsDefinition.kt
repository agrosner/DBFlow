package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.*
import com.raizlabs.android.dbflow.annotation.IndexGroup
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.squareup.javapoet.ParameterizedTypeName
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Description:
 */
class IndexGroupsDefinition(private val tableDefinition: TableDefinition, indexGroup: IndexGroup) {

    val indexName = indexGroup.name
    val indexNumber = indexGroup.number
    val isUnique = indexGroup.unique

    val columnDefinitionList: MutableList<ColumnDefinition> = ArrayList()

    val fieldSpec
        get() = field(ParameterizedTypeName.get(ClassNames.INDEX_PROPERTY, tableDefinition.elementClassName),
                "index_$indexName") {
            addModifiers(public, static, final)
            `=` {
                add("new \$T<>(${indexName.S}, $isUnique, \$T.class",
                        ClassNames.INDEX_PROPERTY, tableDefinition.elementTypeName)

                if (columnDefinitionList.isNotEmpty()) {
                    add(",")
                }
                val index = AtomicInteger(0)
                columnDefinitionList.forEach { it.appendIndexInitializer(this, index) }
                add(")")
            }
        }.build()!!

}
