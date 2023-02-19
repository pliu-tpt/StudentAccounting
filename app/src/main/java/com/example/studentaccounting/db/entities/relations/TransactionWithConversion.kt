package com.example.studentaccounting.db.entities.relations

import androidx.room.Embedded
import com.example.studentaccounting.db.entities.Transaction


data class TransactionWithConversion(
    @Embedded
    val transaction: Transaction,
    val preferred_currency_amount: Double,
    val preferred_currency: String
)
