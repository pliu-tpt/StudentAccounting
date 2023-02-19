package com.example.studentaccounting.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName= "transaction_table")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    var id: Int,

    @ColumnInfo(name = "transaction_name")
    var name: String,

    @ColumnInfo(name = "transaction_category")
    var category: String,

    @ColumnInfo(name = "transaction_subcategory")
    var subcategory: String,

    @ColumnInfo(name = "transaction_type")
    var type: String,

    @ColumnInfo(name = "transaction_amount")
    val amount: Double,
//    var amount: FastMoney,

    @ColumnInfo(name = "transaction_currency")
    val currency: String,

    @ColumnInfo(name = "isSpending")
    val isSpending: Boolean,

    @ColumnInfo(name = "transaction_date")
    var date: String,
)

// One way to implement categories without the hassle of migrating the DB every time a new category
// is introduced is to create a UI that facilitates the selection of a String from
// the already existing list of Strings... (ie from a pure UI standpoint)


