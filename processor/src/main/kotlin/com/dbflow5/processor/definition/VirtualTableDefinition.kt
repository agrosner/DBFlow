package com.dbflow5.processor.definition

import com.dbflow5.annotation.VirtualTable
import com.dbflow5.processor.ProcessorManager
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.dbflow5.processor.utils.annotation
import com.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.dbflow5.processor.utils.isNullOrEmpty
import javax.lang.model.element.TypeElement

/**
 * Description:
 */
class VirtualTableDefinition(virtualTable: VirtualTable,
                             typeElement: TypeElement, processorManager: ProcessorManager)
    : BaseTableDefinition(typeElement, processorManager) {

    private var createWithDatabase = true

    private var type: VirtualTable.Type = VirtualTable.Type.FTS4

    override val associationalBehavior = AssociationalBehavior(
            name = if (virtualTable.name.isNullOrEmpty()) typeElement.simpleName.toString() else virtualTable.name,
            databaseTypeName = virtualTable.extractTypeNameFromAnnotation { it.database },
            allFields = virtualTable.allFields
    )

    override val methods: Array<MethodDefinition> = arrayOf(
            /*BindToStatementMethod(this, BindToStatementMethod.Mode.INSERT),
            BindToStatementMethod(this, BindToStatementMethod.Mode.UPDATE),
            BindToStatementMethod(this, BindToStatementMethod.Mode.DELETE),
            InsertStatementQueryMethod(this, InsertStatementQueryMethod.Mode.INSERT),
            InsertStatementQueryMethod(this, InsertStatementQueryMethod.Mode.SAVE),
            UpdateStatementQueryMethod(this),
            DeleteStatementQueryMethod(this),
            CreationQueryMethod(this),*/ // TODO: reactivate with some refactoring.
            LoadFromCursorMethod(this),
            ExistenceMethod(this),
            PrimaryConditionMethod(this))

    init {
        element.annotation<VirtualTable>()?.let { ftS4 ->
            type = ftS4.type
            createWithDatabase = ftS4.createWithDatabase
        }
    }

    override fun createColumnDefinitions(typeElement: TypeElement) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val primaryColumnDefinitions: List<ColumnDefinition>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun prepareForWrite() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}