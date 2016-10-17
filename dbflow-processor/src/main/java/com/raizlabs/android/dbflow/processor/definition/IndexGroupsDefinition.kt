package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.IndexGroup
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.column.ColumnDefinition
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.ParameterizedTypeName
import java.util.*
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class IndexGroupsDefinition(private val tableDefinition: TableDefinition, indexGroup: IndexGroup) {

    val indexName: String
    val indexNumber: Int
    val isUnique: Boolean

    val columnDefinitionList: MutableList<ColumnDefinition> = ArrayList()

    init {
        this.indexName = indexGroup.name
        this.indexNumber = indexGroup.number
        this.isUnique = indexGroup.unique
    }

    val fieldSpec: FieldSpec
        get() {
            val fieldBuilder = FieldSpec.builder(ParameterizedTypeName.get(ClassNames.INDEX_PROPERTY, tableDefinition.elementClassName),
                    "index_" + indexName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            val initializer = CodeBlock.builder().add("new \$T<>(\$S, \$L, \$T.class", ClassNames.INDEX_PROPERTY,
                    indexName, isUnique, tableDefinition.elementTypeName)

            columnDefinitionList.forEach { initializer.add(", \$L", it.columnName) }
            initializer.add(")")

            fieldBuilder.initializer(initializer.build())

            return fieldBuilder.build()
        }

}
