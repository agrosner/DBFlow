package com.raizlabs.android.dbflow.test.sql.inherited;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.InheritedColumn;
import com.raizlabs.android.dbflow.annotation.InheritedPrimaryKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(database = TestDatabase.class,
        inheritedColumns = {@InheritedColumn(column = @Column, fieldName = "name"),
                @InheritedColumn(column = @Column, fieldName = "number")},
        inheritedPrimaryKeys = {@InheritedPrimaryKey(column = @Column,
                primaryKey = @PrimaryKey,
                fieldName = "inherited_primary_key")})
public class InheritorModel extends InheritedModel implements Model {

    private final ModelAdapter<InheritorModel> modelAdapter = FlowManager.getModelAdapter(InheritorModel.class);

    @Column
    @PrimaryKey
    String primary_key;

    @Override
    public void save() {
        modelAdapter.save(this);
    }

    @Override
    public void delete() {
        modelAdapter.delete(this);
    }

    @Override
    public void update() {
        modelAdapter.update(this);
    }

    @Override
    public void insert() {
        modelAdapter.insert(this);
    }

    @Override
    public boolean exists() {
        return modelAdapter.exists(this);
    }
}
