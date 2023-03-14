package com.example.studentaccounting.db

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.studentaccounting.TransactionListFragment.Companion.MYTAG

public final class DaoUtils {



    companion object {

        fun getQueryFromQueryPair(pair: Pair<String, MutableList<Any>?>): SupportSQLiteQuery {
            Log.i(MYTAG, "${pair.first}, ${pair.second.toString()}")
            return if (pair.second.isNullOrEmpty()) {
                SimpleSQLiteQuery(pair.first)
            } else {
                SimpleSQLiteQuery(pair.first, pair.second!!.toTypedArray())
            }
        }

        fun getFilterConditionsQueryPair(filters: Filters): Pair<String, MutableList<Any>?> {
            val filterConditions = mutableListOf<Pair<String, Any?>>() // used to construct the final query

            with(filters) {
                month.value?.let { if (it != "-1") filterConditions.add("STRFTIME('%m',transaction_date) = ?" to it) }
                year.value?.let { if (it != "-1") filterConditions.add("STRFTIME('%Y',transaction_date) = ?" to it) }
                cat.value?.let { if (it != "-1") filterConditions.add("transaction_category = ?" to it) }
                // "subqueries" to filter  specific field
            }
            if (filterConditions.isEmpty()) {
                return "SELECT * FROM transaction_table" to null // if no filters are used
            } else {
                var conditionsMerged =
                    filterConditions.joinToString(separator = " AND ") { it.first }

                filters.isSorted.value?.let {
                    if (it) conditionsMerged += " ORDER BY -1.0*isSpending, CASE isSpending " +
                            "WHEN 0 THEN transaction_amount ELSE -1.0*transaction_amount END"
                }

                val bindArgs = filterConditions.mapNotNull { it.second }.toMutableList()

                return if (bindArgs.isEmpty()) {
                    "SELECT * FROM transaction_table WHERE $conditionsMerged" to null
                } else {
                    "SELECT * FROM transaction_table WHERE $conditionsMerged" to bindArgs // .toTypedArray()
                }
            }
        }

        fun getFilteredWithPrefCurrencyQueryPair(filters: Filters): Pair<String, MutableList<Any>?>{
            val filteredQuery = getFilterConditionsQueryPair(filters)

            var query : String = "" // to construct the SQL Query
            val bindArgs = mutableListOf<Any>() // to give the necessary arguments

            if (filters.prefCurrency.value.isNullOrEmpty()) {
                Log.i(MYTAG, "Couldn't Aggregate given filter")
                return filteredQuery //
            } else {
                query += "SELECT t.* ,transaction_amount * rate AS preferred_currency_amount, ? AS preferred_currency \n FROM ("
                bindArgs.add(filters.prefCurrency.value!!)

                query += filteredQuery.first
                filteredQuery.second?.let { bindArgs.addAll(it) }

                query +=") AS t LEFT JOIN (SELECT currency_name,  destination / departure AS rate FROM \n" +
                        "       (SELECT currency_name, USD_to_it AS departure, (SELECT USD_to_it FROM currency_table WHERE currency_name=?) AS destination FROM currency_table)) AS c \n" +
                        "       ON t.transaction_currency = c.currency_name"
                bindArgs.add(filters.prefCurrency.value!!)

                if (filters.isSortedByDate){
                    query+="\n ORDER BY transaction_date DESC;"
                }
            }
            return query to bindArgs
        }

        fun getAggregateConditionsQueryPair(filters: Filters): Pair<String, MutableList<Any>?> {
            val filteredQuery = getFilterConditionsQueryPair(filters)

            var query : String = "" // to construct the SQL Query
            val bindArgs = mutableListOf<Any>() // to give the necessary arguments

            if (filters.cat.value == "-1" || filters.cat.value.isNullOrBlank()) {
                if (filters.prefCurrency.value.isNullOrEmpty()) {
                    Log.i(MYTAG, "Couldn't Aggregate given filter")
                    return filteredQuery //
                } else {
                    query +="SELECT transaction_category AS option, SUM(CASE WHEN isSpending = 1 THEN preferred_currency_amount ELSE -preferred_currency_amount END) AS total \n" +
                            "FROM (SELECT t.* ,transaction_amount * rate AS preferred_currency_amount, ? AS preferred_currency \n" +
                            "FROM ("
                    bindArgs.add(filters.prefCurrency.value!!)

                    query += filteredQuery.first
                    filteredQuery.second?.let { bindArgs.addAll(it) }

                    query +=") AS t LEFT JOIN (SELECT currency_name, destination / departure AS rate FROM\n" +
                            "       (SELECT currency_name, USD_to_it AS departure, (SELECT USD_to_it FROM currency_table WHERE currency_name = ?) AS destination FROM currency_table)) AS c \n" +
                            "       ON t.transaction_currency = c.currency_name)\n" +
                            "GROUP BY transaction_category\n" +
                            "ORDER BY total DESC;"
                    bindArgs.add(filters.prefCurrency.value!!)
                }
            } else {
                query +="SELECT transaction_subcategory AS option, SUM(CASE WHEN isSpending = 1 THEN preferred_currency_amount ELSE -preferred_currency_amount END) AS total\n" +
                        "FROM (SELECT t.* ,transaction_amount * rate AS preferred_currency_amount, ? AS preferred_currency \n " +
                        "FROM ("
                bindArgs.add(filters.prefCurrency.value!!)

                query += filteredQuery.first
                filteredQuery.second?.let { bindArgs.addAll(it) }

                query +=") AS t LEFT JOIN (SELECT currency_name,  destination / departure AS rate FROM\n" +
                        "                            (SELECT currency_name, USD_to_it AS departure, (SELECT USD_to_it FROM currency_table WHERE currency_name=?) AS destination FROM currency_table)) AS c \n" +
                        "       ON t.transaction_currency = c.currency_name) \n"
                bindArgs.add(filters.prefCurrency.value!!)

                query +=" WHERE transaction_category =? \n" +
                        " GROUP BY transaction_subcategory \n" +
                        " ORDER BY total DESC;"

                Log.i(MYTAG, "$query $bindArgs")
                Log.i(MYTAG, filters.cat.value!!.toString())
                bindArgs.add(filters.cat.value!!.toString())
            }

            return query to bindArgs
        }

        fun getLineGraphConditionsQueryPair(filters: Filters): Pair<String, MutableList<Any>?> {

            var query : String = "" // to construct the SQL Query
            val bindArgs = mutableListOf<Any>() // to give the necessary arguments

            if (filters.cat.value == "-1" || filters.cat.value.isNullOrBlank()) {
                if (filters.prefCurrency.value.isNullOrEmpty()) {
                    Log.i(MYTAG, "Couldn't Aggregate given filter")
                    return query to bindArgs //
                } else {
                    query +="SELECT transaction_category AS option, STRFTIME('%Y-%m', transaction_date) as month, SUM(CASE WHEN isSpending = 1 THEN preferred_currency_amount ELSE -preferred_currency_amount END) AS total \n" +
                            "FROM (SELECT t.* ,transaction_amount * rate AS preferred_currency_amount, ? AS preferred_currency \n" +
                            "FROM transaction_table"
                    bindArgs.add(filters.prefCurrency.value!!)

                    query +=" AS t LEFT JOIN (SELECT currency_name, destination / departure AS rate FROM\n" +
                            "       (SELECT currency_name, USD_to_it AS departure, (SELECT USD_to_it FROM currency_table WHERE currency_name = ?) AS destination FROM currency_table)) AS c \n" +
                            "       ON t.transaction_currency = c.currency_name)\n" +
                            "GROUP BY transaction_category, month\n" +
                            "HAVING "
                    bindArgs.add(filters.prefCurrency.value!!)

                    if (filters.startMonth.value == "-1"){
                        query += "month >= MIN(month) "
                    } else {
                        query += "month >= ? "
                        bindArgs.add(filters.startMonth.value!!)
                    }
                    if (filters.endMonth.value == "-1"){
                        query += "AND month <= MAX(month)"
                    } else {
                        query += "AND month <= ? "
                        bindArgs.add(filters.endMonth.value!!)
                    }
                    query +=" ORDER BY month ASC;"
                }
            } else {
                query +="SELECT transaction_category AS option, STRFTIME('%Y-%m', transaction_date) as month, SUM(CASE WHEN isSpending = 1 THEN preferred_currency_amount ELSE -preferred_currency_amount END) AS total \n" +
                        "FROM (SELECT t.* ,transaction_amount * rate AS preferred_currency_amount, ? AS preferred_currency \n" +
                        "FROM transaction_table"
                bindArgs.add(filters.prefCurrency.value!!)

                query +=" AS t LEFT JOIN (SELECT currency_name, destination / departure AS rate FROM\n" +
                        "       (SELECT currency_name, USD_to_it AS departure, (SELECT USD_to_it FROM currency_table WHERE currency_name = ?) AS destination FROM currency_table)) AS c \n" +
                        "       ON t.transaction_currency = c.currency_name)\n" +
                        "GROUP BY transaction_category, month \n" +
                        "HAVING "
                bindArgs.add(filters.prefCurrency.value!!)

                query += "option = ? "
                bindArgs.add(filters.cat.value!!)

                if (filters.startMonth.value == "-1"){
                    query += "AND month >= MIN(month) "
                } else {
                    query += "AND month >= ? "
                    bindArgs.add(filters.startMonth.value!!)
                }
                if (filters.endMonth.value == "-1"){
                    query += "AND month <= MAX(month)"
                } else {
                    query += "AND month <= ? "
                    bindArgs.add(filters.endMonth.value!!)
                }
                query +=" ORDER BY month ASC;"
            }
            return query to bindArgs
        }
    }
}