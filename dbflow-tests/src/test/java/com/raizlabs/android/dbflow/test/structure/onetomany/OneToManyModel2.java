package com.raizlabs.android.dbflow.test.structure.onetomany;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.List;

@Table(database = TestDatabase.class)
public class OneToManyModel2 extends BaseModel {

    List<TaskTag> tags;

    @PrimaryKey
    String id;

    @Column
    public String text;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "tags")
    public List<TaskTag> getRewards() {
        if (tags == null) {
            tags = SQLite.select()
                    .from(TaskTag.class)
                    .queryList();
        }
        return tags;
    }

}
