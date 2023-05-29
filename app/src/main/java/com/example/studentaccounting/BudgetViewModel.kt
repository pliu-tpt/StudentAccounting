package com.example.studentaccounting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studentaccounting.db.Filters
import com.example.studentaccounting.db.entities.relations.OptionWithTotal
import com.example.studentaccounting.db.entities.relations.TransactionWithConversion
import java.time.LocalDate
import java.util.*

class BudgetViewModel: ViewModel() {
    var filteredTransactions = MutableLiveData<List<TransactionWithConversion>>() // each transaction, converted
    var optionWithTotal = MutableLiveData<List<OptionWithTotal>>() // each aggregate, converted

    var filters = Filters()

    var date = Date()

    init {
        filters.month.value = String.format("%02d", LocalDate.now().monthValue)  // Default value at init "-1"
        filters.year.value = LocalDate.now().year.toString()
        filters.cat.value = "-1"
        filters.isSorted.value = true
        filters.prefCurrency.value = "SGD"
    }

    fun updateMonthAndYear(month:Int?, year:Int?){
        filters.month.value = String.format("%02d", month)
        filters.year.value = String.format("%02d", year)
    }

    fun updateCat(cat:String?) {
        filters.cat.value = cat
    }

    fun updateOptionWithTotal(optionWithTotals: List<OptionWithTotal>){
        optionWithTotal.value = optionWithTotals
    }

    fun updateFilteredTransactions(transactions:List<TransactionWithConversion>){
        filteredTransactions.value = transactions
    }

    fun updateDate(d:Date){
        date = d
    }

    fun updateIsSorted(boolean: Boolean){
        filters.isSorted.value = boolean
    }

    fun updatePrefCurrency(preferredCurrency: String){
        filters.prefCurrency.value = preferredCurrency
    }
}