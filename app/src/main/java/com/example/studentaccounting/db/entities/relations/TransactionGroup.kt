package com.example.studentaccounting.db.entities.relations

data class TransactionGroup(
    val option: String,
    val total: Double,
    val transactions: List<TransactionWithConversion>,
)