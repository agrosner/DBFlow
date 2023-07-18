package com.dbflow5.rx2.query

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table
class SimpleRXModel(@PrimaryKey var id: String = "")
