package com.example.studentaccounting.db.entities.relations

data class AmountWithCurrency(
    val transaction_id : Int,
    val transaction_amount: Double,
    val transaction_currency: String,
    val isSpending : Boolean
)
