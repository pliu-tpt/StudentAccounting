package com.example.studentaccounting.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.studentaccounting.db.entities.Currency


@Dao
interface CurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(currency: Currency)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(currency: List<Currency?>?)

    @Update
    suspend fun updateCurrency(currency: Currency)

    @Delete
    suspend fun deleteCurrency(currency: Currency)

    @Query("SELECT * FROM currency_table")
    fun getAllCurrencies(): LiveData<List<Currency>> // separate co.rout.
}