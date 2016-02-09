package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.processor.ClassNames;
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition;
import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * Description: Generates the Model class that is used in a many to many.
 */
public class ManyToManyDefinition extends BaseDefinition {

    TypeName referencedTable;

    public ManyToManyDefinition(TypeElement element, ProcessorManager processorManager) {
        super(element, processorManager);

        ManyToMany manyToMany = element.getAnnotation(ManyToMany.class);
        referencedTable = TypeName.get(ModelUtils.getReferencedClassFromAnnotation(manyToMany));

        TypeName databaseTypeName = null;
        Table table = element.getAnnotation(Table.class);
        try {
            table.database();
        } catch (MirroredTypeException mte) {
            databaseTypeName = TypeName.get(mte.getTypeMirror());
        }

        DatabaseDefinition databaseDefinition = manager.getDatabaseWriter(databaseTypeName);
        if (databaseDefinition == null) {
            manager.logError("DatabaseDefinition was null for : " + elementName);
        } else {
            setOutputClassName(databaseDefinition.classSeparator + referencedTable);
        }
    }

    @Override
    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {

    }

    @Override
    protected TypeName getExtendsClass() {
        return ClassNames.BASE_MODEL;
    }
}
