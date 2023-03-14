package com.example.studentaccounting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentaccounting.TransactionListFragment.Companion.MYTAG
import com.example.studentaccounting.db.CurrencyDao
import com.example.studentaccounting.db.Filters
import com.example.studentaccounting.db.TransactionDao
import com.example.studentaccounting.db.entities.Currency
import com.example.studentaccounting.db.entities.Transaction
import com.example.studentaccounting.db.entities.relations.OptionWithDateAndTotal
import com.example.studentaccounting.db.entities.relations.OptionWithTotal
import com.example.studentaccounting.db.entities.relations.TransactionWithConversion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.money.Monetary
import javax.money.MonetaryException
import javax.money.convert.MonetaryConversions

class TransactionViewModel(private val dao:TransactionDao, private val currencyDao: CurrencyDao): ViewModel() {

    var transactions : LiveData<List<Transaction>> = dao.getAllTransactions()
    var transactionTypes : LiveData<List<String>> = dao.getAllTransactionsType()
    var categories : LiveData<List<String>> = dao.getAllCategory()
    var subcategories : LiveData<List<String>> = dao.getAllSubcategory()
    var currencies : LiveData<List<String>> = dao.getAllCurrency()

    var selectedType = MutableLiveData<String>()
    var selectedCat = MutableLiveData<String>()
    var selectedSubcat = MutableLiveData<String>()
    var selectedCurrency = MutableLiveData<String>()
    var selectedAmount = MutableLiveData<Double>()
    var selectedDate = MutableLiveData<Calendar>()
    var selectedName = MutableLiveData<String>()
    var selectedIsSpending = MutableLiveData<Boolean>()

    var isTransfer = false
    var selectedTypeFrom = MutableLiveData<String>()
    var selectedTypeTo = MutableLiveData<String>()

    var selectedSubcategories : List<String>?=null

    var preferredCurrency = MutableLiveData<String>()

    val formatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    var transactionToEdit = MutableLiveData<Transaction>()

    init {
        initSelection()
    }

    fun updateTransactionToEdit(transaction: Transaction) {
        transactionToEdit.value = transaction
    }

    fun editTransaction(transaction: Transaction) = viewModelScope.launch {
//        insertCurrencyToDao(transaction.currency)
        dao.updateTransaction(transaction)
        Log.i(MYTAG, "Transaction ${transaction.id} modified: ${transaction.name}, ${transaction.amount}, ${transaction.currency}")
    }

    // The following is executed SEQUENTIALLY by using suspend for both insertCurrencyToDao and dao.insertTransaction
    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        insertCurrencyToDao(transaction.currency) // if the currency doesn't exist
        dao.insertTransaction(transaction)
    }

    // The following is executed SEQUENTIALLY by using suspend for both insertCurrencyToDao and dao.insertTransaction
    fun insertTransactionAnyID(transaction: Transaction) = viewModelScope.launch {
        insertCurrencyToDao(transaction.currency)
        dao.insertTransaction(
                Transaction(
                    0,
                    transaction.name,
                    transaction.category,
                    transaction.subcategory,
                    transaction.type,
                    transaction.amount,
                    transaction.currency,
                    transaction.isSpending,
                    transaction.date)
        )
    }

    fun insertSelectedTransaction() = viewModelScope.launch {
        if (!isTransfer) {
            insertTransaction(
                Transaction(
                    0,
                    selectedName.value!!,
                    selectedCat.value!!,
                    selectedSubcat.value!!,
                    selectedType.value!!,
                    selectedAmount.value!!,
                    selectedCurrency.value!!,
                    selectedIsSpending.value!!,
                    formatter.format(selectedDate.value!!.time)
                )
            )
        } else {
            insertTransaction(
                Transaction(
                    0,
                    selectedName.value!!,
                    selectedCat.value!!,
                    selectedSubcat.value!!,
                    selectedTypeFrom.value!!,
                    selectedAmount.value!!,
                    selectedCurrency.value!!,
                    true,
                    formatter.format(selectedDate.value!!.time))
            )
            insertTransaction(
                Transaction(
                0,
                selectedName.value!!,
                selectedCat.value!!,
                selectedSubcat.value!!,
                selectedTypeTo.value!!,
                selectedAmount.value!!,
                selectedCurrency.value!!,
                false,
                formatter.format(selectedDate.value!!.time))
            )

        }
        initSelection()
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        dao.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        dao.deleteTransaction(transaction)
    }

    private fun initDao(){
        transactions = dao.getAllTransactions()
        transactionTypes = dao.getAllTransactionsType()
        categories = dao.getAllCategory()
        subcategories = dao.getAllSubcategory()
        currencies = dao.getAllCurrency()
    }

    private fun initSelection() {
        selectedType.value = ""
        selectedCat.value = ""
        selectedSubcat.value = ""

        selectedAmount.value = 0.0
        selectedDate.value = Calendar.getInstance()
        selectedName.value = ""
        selectedIsSpending.value = true

        isTransfer = false
        selectedTypeFrom.value = ""
        selectedTypeTo.value = ""

        preferredCurrency.value = "SGD"
        selectedCurrency.value = preferredCurrency.value
    }

    fun updateIsSpending(bool:Boolean){
        selectedIsSpending.value = bool
    }

    fun updateSelectedName(name:String){
        selectedName.value = name
    }

    suspend fun updateSelectedCat(cat:String){
        selectedCat.postValue(cat)
        selectedSubcategories = dao.getSubcategory(cat) // list of subcats given the category
    }

    suspend fun getTransactionsByMonthAndYear(month: Int, year: Int): List<Transaction> {
        return dao.getTransactionsByMonthAndYear(
            "%02d".format(month),
            "%02d".format(year)
        )
    }

    suspend fun getTypeAggregate(): List<OptionWithTotal>? {
        return preferredCurrency.value?.let { dao.getTypeAggregate(it) }
    }

    suspend fun getAllFiltered(filters:Filters): List<Transaction>? {
        return dao.getAllFiltered(filters)
    }

    suspend fun getAllFilteredAggregated(filters:Filters): List<OptionWithTotal>? {
        return dao.getAllFilteredAggregated(filters)
    }

    suspend fun getAllFilteredLineGraph(filters:Filters): List<OptionWithDateAndTotal>? {
        return dao.getAllFilteredLineGraph(filters)
    }

    suspend fun getAllFilteredWithPrefCurrency(filters:Filters): List<TransactionWithConversion>?{
        return dao.getAllFilteredWithPrefCurrency(filters)
    }

    private suspend fun insertCurrencyToDao(currency: String){

        try {
            val oneDollar =
                Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(1).create()
            val conversionNewCurrency = MonetaryConversions.getConversion(currency, "IMF")
            val convertedAmount = oneDollar.with(conversionNewCurrency).number.toDouble()
            currencyDao.insertCurrency(
                Currency(
                    currency,
                    convertedAmount
                )
            )
        } catch (e: MonetaryException) {
            Log.i(MYTAG, "Failure: Currency DOESN'T EXIST")
        }

    }

    fun updateSelectedSubcat(subcat:String){
        selectedSubcat.value = subcat
    }

    fun updateSelectedCurrency(currency:String){
        selectedCurrency.value = currency
    }

    fun updatePreferredCurrency(currency:String){
        preferredCurrency.value = currency
    }

    fun updateSelectedAmount(amount:Double){
        selectedAmount.value = amount
    }

    fun updateSelectedType(type:String){
        selectedType.value = type
    }

    fun updateSelectedTypeFrom(type:String){
        selectedTypeFrom.value = type
    }

    fun updateSelectedTypeTo(type:String){
        selectedTypeTo.value = type
    }

    fun updateIsTransfer(bool: Boolean){
        isTransfer = bool
    }

}