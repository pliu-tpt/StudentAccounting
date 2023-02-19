package com.example.studentaccounting.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName= "currency_table")
data class Currency(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "currency_name")
    var name: String,

    @ColumnInfo(name = "USD_to_it")
    var USDtoIt: Double, // e.g. 1 USD = 0.93 EUR (usd to curr to have large numbers instead of 0.0000X)
)



