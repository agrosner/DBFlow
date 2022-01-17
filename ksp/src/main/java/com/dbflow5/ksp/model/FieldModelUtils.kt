package com.dbflow5.ksp.model

import com.dbflow5.codegen.model.ClassModel
import com.dbflow5.codegen.model.ReferenceHolderModel
import com.dbflow5.codegen.model.SingleFieldModel
import com.dbflow5.codegen.model.cache.ReferencesCache
import com.dbflow5.ksp.writer.FieldExtractor

fun SingleFieldModel.toExtractor(classModel: ClassModel) = FieldExtractor.SingleFieldExtractor(
    this,
    classModel,
)

fun ReferenceHolderModel.toExtractor(
    classModel: ClassModel,
    referencesCache: ReferencesCache
) = FieldExtractor.ForeignFieldExtractor(
    this,
    referencesCache,
    classModel
)
