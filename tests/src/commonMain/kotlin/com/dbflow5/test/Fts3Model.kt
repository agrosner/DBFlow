package com.dbflow5.test

import com.dbflow5.annotation.Fts3
import com.dbflow5.annotation.Table

@Table
@Fts3
data class Fts3Model(val name: String)

