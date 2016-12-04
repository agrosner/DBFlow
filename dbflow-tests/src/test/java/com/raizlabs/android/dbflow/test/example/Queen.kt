package com.raizlabs.android.dbflow.test.example

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description:
 */
@Table(database = ColonyDatabase::class)
class Queen : BaseModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var name: String? = null

    @Column
    @ForeignKey
    var colony: Colony? = null

    var ants: List<Ant>? = null

    val myAnts: List<Ant>
        @OneToMany(methods = arrayOf(OneToMany.Method.ALL), variableName = "ants",
            isVariablePrivate = true)
        get() {
            if (ants == null || ants!!.isEmpty()) {
                ants = SQLite.select()
                    .from(Ant::class.java)
                    .where(Ant_Table.queen_id.eq(id))
                    .queryList()
            }
            return ants as List<Ant>
        }

}
