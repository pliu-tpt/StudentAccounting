package com.example.studentaccounting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studentaccounting.db.Filters
import com.example.studentaccounting.db.entities.relations.OptionWithDateAndTotal
import java.util.*

class TimeSeriesViewModel: ViewModel() {
    var lineGraph = MutableLiveData<List<OptionWithDateAndTotal>>()

    var filters = Filters()

    var date = Date()

    init {
        filters.cat.value = "-1"
        filters.isSorted.value = true
        filters.prefCurrency.value = "SGD"

        filters.startMonth.value = "-1"
        filters.endMonth.value = "-1"
    }

    fun updateStartMonthAndYear(monthAndYear:String){
        filters.startMonth.value = monthAndYear
    }

    fun updateEndMonthAndYear(monthAndYear:String){
        filters.endMonth.value = monthAndYear
    }

    fun updateCat(cat:String?) {
        filters.cat.value = cat
    }

    fun updateLineGraph(optionWithDateAndTotal: List<OptionWithDateAndTotal>){
        lineGraph.value = optionWithDateAndTotal
    }

    fun updateDate(d: Date){
        date = d
    }

    fun updateIsSorted(boolean: Boolean){
        filters.isSorted.value = boolean
    }

    fun updatePrefCurrency(preferredCurrency: String){
        filters.prefCurrency.value = preferredCurrency
    }

}