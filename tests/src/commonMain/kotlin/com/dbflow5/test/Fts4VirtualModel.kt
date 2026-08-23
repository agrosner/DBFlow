package com.dbflow5.test

import com.dbflow5.annotation.Fts4
import com.dbflow5.annotation.Table

@Table
@Fts4(contentTable = Fts4ContentModel::class)
data class Fts4VirtualModel(val name: String)
