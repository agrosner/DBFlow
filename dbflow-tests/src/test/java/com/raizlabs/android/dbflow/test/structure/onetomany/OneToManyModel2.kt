package com.raizlabs.android.dbflow.test.structure.onetomany

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.OneToMany
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(database = TestDatabase::class)
class OneToManyModel2 : BaseModel() {

    var tags: List<TaskTag>? = null

    /**
     * @return the id
     */
    /**
     * @param id the id to set
     */
    @PrimaryKey
    var id: String? = null

    /**
     * @return the text
     */
    /**
     * @param text the text to set
     */
    @Column
    var text: String? = null

    val rewards: List<TaskTag>
        @OneToMany(methods = arrayOf(OneToMany.Method.ALL), variableName = "tags",
            isVariablePrivate = true)
        get() {
            var localTags = tags
            if (localTags == null) {
                localTags = SQLite.select()
                    .from(TaskTag::class.java)
                    .queryList()
            }
            tags = localTags
            return localTags
        }

}
