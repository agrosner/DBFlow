package com.raizlabs.android.dbflow.processor.definition.method

import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.TableDefinition
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import java.util.*
import javax.lang.model.element.Modifier

/**
 * Description:
 */
class CreationQueryMethod(private val tableDefinition: TableDefinition) : MethodDefinition {

    override val methodSpec: MethodSpec
        get() {
            val methodBuilder = MethodSpec.methodBuilder("getCreationQuery")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(ClassName.get(String::class.java))

            val creationBuilder = CodeBlock.builder().add("CREATE TABLE IF NOT EXISTS ")
                    .add(QueryBuilder.quote(tableDefinition.tableName)).add("(")

            (0..tableDefinition.columnDefinitions.size - 1).forEach { i ->
                if (i > 0) {
                    creationBuilder.add(",")
                }
                creationBuilder.add(tableDefinition.columnDefinitions[i].creationName)
            }

            tableDefinition.uniqueGroupsDefinitions.forEach {
                if (!it.columnDefinitionList.isEmpty()) creationBuilder.add(it.creationName)
            }

            if (!tableDefinition.hasAutoIncrement) {
                val primarySize = tableDefinition.primaryColumnDefinitions.size
                for (i in 0..primarySize - 1) {
                    if (i == 0) {
                        creationBuilder.add(", PRIMARY KEY(")
                    }

                    if (i > 0) {
                        creationBuilder.add(",")
                    }

                    val primaryDefinition = tableDefinition.primaryColumnDefinitions[i]
                    creationBuilder.add(primaryDefinition.primaryKeyName)

                    if (i == primarySize - 1) {
                        creationBuilder.add(")")
                        if (!tableDefinition.primaryKeyConflictActionName.isNullOrEmpty()) {
                            creationBuilder.add(" ON CONFLICT " + tableDefinition.primaryKeyConflictActionName)
                        }
                    }
                }
            }

            val foreignSize = tableDefinition.foreignKeyDefinitions.size

            val foreignKeyBlocks = ArrayList<CodeBlock>()
            val tableNameBlocks = ArrayList<CodeBlock>()
            val referenceKeyBlocks = ArrayList<CodeBlock>()

            for (i in 0..foreignSize - 1) {
                val foreignKeyBuilder = CodeBlock.builder()
                val referenceBuilder = CodeBlock.builder()
                val foreignKeyColumnDefinition = tableDefinition.foreignKeyDefinitions[i]

                foreignKeyBuilder.add(", FOREIGN KEY(")

                (0..foreignKeyColumnDefinition._foreignKeyReferenceDefinitionList.size - 1).forEach { j ->
                    if (j > 0) {
                        foreignKeyBuilder.add(",")
                    }
                    val referenceDefinition = foreignKeyColumnDefinition._foreignKeyReferenceDefinitionList[j]
                    foreignKeyBuilder.add("\$L", QueryBuilder.quote(referenceDefinition.columnName))
                }


                foreignKeyBuilder.add(") REFERENCES ")

                foreignKeyBlocks.add(foreignKeyBuilder.build())

                tableNameBlocks.add(CodeBlock.builder().add("\$T.getTableName(\$T.class)",
                        ClassNames.FLOW_MANAGER, foreignKeyColumnDefinition.referencedTableClassName).build())

                referenceBuilder.add("(")
                for (j in 0..foreignKeyColumnDefinition._foreignKeyReferenceDefinitionList.size - 1) {
                    if (j > 0) {
                        referenceBuilder.add(", ")
                    }
                    val referenceDefinition = foreignKeyColumnDefinition._foreignKeyReferenceDefinitionList[j]
                    referenceBuilder.add("\$L", QueryBuilder.quote(referenceDefinition.foreignColumnName))
                }
                referenceBuilder.add(") ON UPDATE \$L ON DELETE \$L", foreignKeyColumnDefinition.onUpdate.name.replace("_", " "),
                        foreignKeyColumnDefinition.onDelete.name.replace("_", " "))
                referenceKeyBlocks.add(referenceBuilder.build())
            }

            val codeBuilder = CodeBlock.builder().add("return \$S", creationBuilder.build().toString())

            if (foreignSize > 0) {
                for (i in 0..foreignSize - 1) {
                    codeBuilder.add("+ \$S + \$L + \$S", foreignKeyBlocks[i], tableNameBlocks[i], referenceKeyBlocks[i])
                }
            }
            codeBuilder.add(" + \$S", ");").add(";\n")


            methodBuilder.addCode(codeBuilder.build())

            return methodBuilder.build()
        }
}
