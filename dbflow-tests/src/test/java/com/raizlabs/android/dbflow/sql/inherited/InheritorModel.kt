package com.raizlabs.android.dbflow.sql.inherited

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.InheritedColumn
import com.raizlabs.android.dbflow.annotation.InheritedPrimaryKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class, inheritedColumns = arrayOf(InheritedColumn(column = Column(), fieldName = "name"), InheritedColumn(column = Column(), fieldName = "number")), inheritedPrimaryKeys = arrayOf(InheritedPrimaryKey(column = Column(), primaryKey = PrimaryKey(), fieldName = "inherited_primary_key")))
class InheritorModel : InheritedModel() {

    @Column
    @PrimaryKey
    var primary_key: String? = null

}
