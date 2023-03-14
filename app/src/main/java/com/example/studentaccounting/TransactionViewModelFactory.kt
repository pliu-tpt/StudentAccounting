package com.example.studentaccounting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.studentaccounting.db.CurrencyDao
import com.example.studentaccounting.db.TransactionDao

class TransactionViewModelFactory(
    private val dao: TransactionDao,
    private val currencyDao: CurrencyDao
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)){
            return TransactionViewModel(dao, currencyDao) as T
        }
        throw java.lang.IllegalArgumentException("Unknown View Model Class")
    }

}