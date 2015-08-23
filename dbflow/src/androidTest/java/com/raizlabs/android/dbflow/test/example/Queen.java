package com.raizlabs.android.dbflow.test.example;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

import static com.raizlabs.android.dbflow.sql.builder.Condition.column;

/**
 * Description:
 */
@Table(databaseName = ColonyDatabase.NAME)
public class Queen extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "colony_id",
            columnType = Long.class,
            foreignKeyColumnName = "id")},
            saveForeignKeyModel = false)
    Colony colony;

    List<Ant> ants;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "ants")
    public List<Ant> getMyAnts() {
        if (ants == null) {
            ants = new Select()
                    .from(Ant.class)
                    .where(column(Ant$Table.QUEENFOREIGNKEYCONTAINER_QUEEN_ID).eq(id))
                    .queryList();
        }
        return ants;
    }
}
