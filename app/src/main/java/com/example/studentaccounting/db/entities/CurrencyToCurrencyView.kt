package com.example.studentaccounting.db.entities
//
//import androidx.room.ColumnInfo
//import androidx.room.DatabaseView
//import androidx.room.PrimaryKey
//import androidx.room.Query


// "SELECT t1.name AS destination, t2.name AS departure, (t1.USD_to_it/t2.USD_to_it) AS rate from currency_table AS t1 CROSS JOIN currency_table AS t2"
//@DatabaseView("SELECT *, (t1.USD_to_it/t2.USD_to_it) AS rate from currency_table AS t1 CROSS JOIN currency_table AS t2")
//data class CurrencyToCurrencyView(
//    @PrimaryKey(autoGenerate = false)
//    @ColumnInfo(name = "destination")
//    var destination: String,
//
//    @PrimaryKey(autoGenerate = false)
//    @ColumnInfo(name = "departure")
//    var departure: String,
//
//    @ColumnInfo(name = "rate")
//    var rate: Double, // rate is in destination / departure, multiply by departure to get destination amount
//)



