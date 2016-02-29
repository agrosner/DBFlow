package com.raizlabs.android.dbflow.test.example;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

/**
 * Description:
 */
@ModelContainer
@Table(database = ColonyDatabase.class)
public class Queen extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;

    @Column
    @ForeignKey(saveForeignKeyModel = false)
    Colony colony;

    List<Ant> ants;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "ants")
    public List<Ant> getMyAnts() {
        if (ants == null || ants.isEmpty()) {
            ants = SQLite.select()
                .from(Ant.class)
                .where(Ant_Table.queenForeignKeyContainer_id.eq(id))
                .queryList();
        }
        return ants;
    }
}
