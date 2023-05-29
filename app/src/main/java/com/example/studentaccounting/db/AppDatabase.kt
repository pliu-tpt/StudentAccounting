package com.example.studentaccounting.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.studentaccounting.db.entities.Currency
import com.example.studentaccounting.db.entities.Option
import com.example.studentaccounting.db.entities.Transaction

@Database(entities = [Transaction::class, Currency::class, Option::class], version = 5,  views = [], exportSchema = true)
abstract class AppDatabase : RoomDatabase(){

    abstract fun transactionDao(): TransactionDao
    abstract fun currencyDao() : CurrencyDao
    abstract fun optionDao() : OptionDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context:Context):AppDatabase?{
            synchronized(this){
                var instance = INSTANCE
                if (INSTANCE==null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "application_database"
                    )
                        .allowMainThreadQueries() // this allows Main thread Queries, to use carefully and only light queries should be used.
                        .build()
                }
                return instance
            }
        }
    }
}