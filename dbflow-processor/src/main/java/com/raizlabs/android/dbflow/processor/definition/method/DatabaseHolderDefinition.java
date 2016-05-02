package com.raizlabs.android.dbflow.processor.definition.method;

import com.raizlabs.android.dbflow.processor.definition.ManyToManyDefinition;
import com.raizlabs.android.dbflow.processor.definition.ModelViewDefinition;
import com.raizlabs.android.dbflow.processor.definition.QueryModelDefinition;
import com.raizlabs.android.dbflow.processor.definition.TableDefinition;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: Provides overarching holder for {@link DatabaseDefinition}, {@link TableDefinition},
 * and more. So we can safely use.
 */
public class DatabaseHolderDefinition {

    private DatabaseDefinition databaseDefinition;

    public Map<TypeName, TableDefinition> tableDefinitionMap = new HashMap<>();
    public Map<String, TableDefinition> tableNameMap = new HashMap<>();

    public Map<TypeName, QueryModelDefinition> queryModelDefinitionMap = new HashMap<>();
    public Map<TypeName, ModelViewDefinition> modelViewDefinitionMap = new HashMap<>();
    public Map<TypeName, ManyToManyDefinition> manyToManyDefinitionMap = new HashMap<>();

    public void setDatabaseDefinition(DatabaseDefinition databaseDefinition) {
        this.databaseDefinition = databaseDefinition;
        this.databaseDefinition.setHolderDefinition(this);
    }

    public DatabaseDefinition getDatabaseDefinition() {
        return databaseDefinition;
    }
}
