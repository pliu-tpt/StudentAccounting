package com.example.studentaccounting.db

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.studentaccounting.db.entities.Currency
import com.example.studentaccounting.db.entities.Transaction
import com.example.studentaccounting.db.entities.relations.OptionWithDateAndTotal
import com.example.studentaccounting.db.entities.relations.OptionWithTotal
import com.example.studentaccounting.db.entities.relations.TransactionWithConversion


@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction?>?)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transaction_table ORDER BY transaction_date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>> // separate co.rout.

    @Query("SELECT * FROM transaction_table ORDER BY transaction_date DESC")
    suspend fun getAllTransactionsList(): List<Transaction> // separate co.rout.

    @Query(
        "SELECT transaction_type " +
                "FROM (SELECT transaction_type, COUNT(*) " +
                    "FROM transaction_table " +
                    "GROUP BY transaction_type " +
                    "ORDER BY COUNT(*) DESC)"
    )
    fun getAllTransactionsType(): LiveData<List<String>> // separate co.rout.

    @Query(
        "SELECT transaction_category " +
                "FROM (SELECT transaction_category, COUNT(*) " +
                    "FROM transaction_table " +
                    "GROUP BY transaction_category " +
                    "ORDER BY COUNT(*) DESC)"
    )
    fun getAllCategory(): LiveData<List<String>> // separate co.rout.

    @Query(
        "SELECT transaction_subcategory " +
                "FROM (SELECT transaction_subcategory, COUNT(*) " +
                    "FROM transaction_table " +
                    "GROUP BY transaction_subcategory " +
                    "ORDER BY COUNT(*) DESC)"
    )
    fun getAllSubcategory(): LiveData<List<String>> // separate co.rout.

    @Query("SELECT DISTINCT transaction_subcategory FROM transaction_table WHERE transaction_category = :category")
    suspend fun getSubcategory(category: String): List<String>

    @Query("SELECT DISTINCT transaction_currency FROM transaction_table")
    fun getAllCurrency(): LiveData<List<String>> // separate co.rout.

    @Query("SELECT * FROM transaction_table " +
            "WHERE STRFTIME('%Y',transaction_date) = :year AND STRFTIME('%m',transaction_date) = :month")
    suspend fun getTransactionsByMonthAndYear(month: String, year: String): List<Transaction>


//    @Query("SELECT transaction_id, transaction_amount * rate AS transaction_amount, :preferredCurrency AS transaction_currency, isSpending " +
//            "FROM transaction_table AS t LEFT JOIN CurrencyToCurrencyView AS c ON transaction_currency = c.currency_name AND c.`currency_name:1` =:preferredCurrency")
//    suspend fun getTransactionsToPreferredCurrency(preferredCurrency: String) : List<AmountWithCurrency>
//
//    @Query("SELECT transaction_subcategory AS option, SUM(CASE WHEN isSpending = 1 THEN transaction_amount ELSE -transaction_amount END) AS total\n" +
//            "FROM (SELECT transaction_id, transaction_category, transaction_subcategory, transaction_amount * rate AS transaction_amount, :preferredCurrency AS transaction_currency, isSpending\n" +
//            "            FROM transaction_table AS t \n" +
//            "            LEFT JOIN CurrencyToCurrencyView AS c ON transaction_currency = c.currency_name AND c.`currency_name:1` =:preferredCurrency)\n" +
//            "WHERE transaction_category = :category\n" +
//            "GROUP BY transaction_subcategory\n" +
//            "ORDER BY total DESC")
//    suspend fun getSubcatAggregateGivenCat(category: String, preferredCurrency: String): List<OptionWithTotal>
//    // get for each subcategory (of a cat) a sum after having converted the currencies to the preferredCurrency (the amount is the amount SPENT)
//
//    @Query("SELECT transaction_category AS option, SUM(CASE WHEN isSpending = 1 THEN transaction_amount ELSE -transaction_amount END) AS total\n" +
//            "FROM (SELECT transaction_id, transaction_category, transaction_amount * rate AS transaction_amount, :preferredCurrency AS transaction_currency, isSpending\n" +
//            "            FROM transaction_table AS t \n" +
//            "            LEFT JOIN CurrencyToCurrencyView AS c ON transaction_currency = c.currency_name AND c.`currency_name:1` =:preferredCurrency)\n" +
//            "GROUP BY transaction_category\n" +
//            "ORDER BY total DESC")
//    suspend fun getCatAggregate(preferredCurrency: String): List<OptionWithTotal>
    // get for each category a sum after having converted the currencies to the preferredCurrency (the amount is the amount SPENT)

    @Query("SELECT transaction_type AS option, SUM(CASE WHEN isSpending = 1 THEN preferred_currency_amount ELSE -preferred_currency_amount END) AS total \n" +
            "FROM (SELECT t.* ,transaction_amount * rate AS preferred_currency_amount, :preferredCurrency AS preferred_currency \n" +
            "FROM transaction_table as t LEFT JOIN (SELECT currency_name, destination / departure AS rate FROM\n" +
            "                                   (SELECT currency_name, USD_to_it AS departure, (SELECT USD_to_it FROM currency_table WHERE currency_name = :preferredCurrency) AS destination FROM currency_table)) AS c\n" +
            "                                   ON t.transaction_currency = c.currency_name)\n" +
            "                            GROUP BY transaction_type\n" +
            "                            ORDER BY total DESC;")
    suspend fun getTypeAggregate(preferredCurrency: String): List<OptionWithTotal>

    @RawQuery(observedEntities = [Transaction::class, Currency::class])
    suspend fun getAllFilteredAggregated(query: SupportSQLiteQuery): List<OptionWithTotal>?

    suspend fun getAllFilteredAggregated(filters: Filters): List<OptionWithTotal>? {
        return getAllFilteredAggregated(DaoUtils.getQueryFromQueryPair(DaoUtils.getAggregateConditionsQueryPair(filters)))
    }

    @RawQuery(observedEntities = [Transaction::class, Currency::class])
    suspend fun getAllFilteredLineGraph(query: SupportSQLiteQuery): List<OptionWithDateAndTotal>?

    suspend fun getAllFilteredLineGraph(filters: Filters): List<OptionWithDateAndTotal>? {
        return getAllFilteredLineGraph(DaoUtils.getQueryFromQueryPair(DaoUtils.getLineGraphConditionsQueryPair(filters)))
    }

    @RawQuery(observedEntities = [Transaction::class])
    suspend fun getAllFiltered(query: SupportSQLiteQuery): List<Transaction>?

    suspend fun getAllFiltered(filters: Filters): List<Transaction>? {
        return getAllFiltered(DaoUtils.getQueryFromQueryPair(DaoUtils.getFilterConditionsQueryPair(filters)))
    }

    @RawQuery(observedEntities = [Transaction::class, Currency::class])
    suspend fun getAllFilteredWithPrefCurrency(query: SupportSQLiteQuery): List<TransactionWithConversion>?

    suspend fun getAllFilteredWithPrefCurrency(filters: Filters): List<TransactionWithConversion>? {
        return getAllFilteredWithPrefCurrency(DaoUtils.getQueryFromQueryPair(DaoUtils.getFilteredWithPrefCurrencyQueryPair(filters)))
    }
}