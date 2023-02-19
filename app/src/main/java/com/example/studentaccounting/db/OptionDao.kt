package com.example.studentaccounting.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.studentaccounting.db.entities.Currency
import com.example.studentaccounting.db.entities.Option


@Dao
interface OptionDao {
    @Insert
    suspend fun insertOption(option: Option)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(option: List<Option?>?)

    @Update
    suspend fun updateOption(option: Option)

    @Delete
    suspend fun deleteOption(option: Option)

    @Query("SELECT * FROM option_table")
    fun getAllOptions(): LiveData<List<Option>> // separate co.rout.
}