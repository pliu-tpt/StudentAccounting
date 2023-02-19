//package com.example.studentaccounting.db
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.example.studentaccounting.db.entities.Transaction
//
//@Database(entities = [Transaction::class], version = 2, exportSchema = true)
//abstract class TransactionDatabase : RoomDatabase(){
//
//    abstract fun transactionDao(): TransactionDao
//
//    abstract fun currencyDao() : CurrencyDao
//
//    companion object{
//        @Volatile
//        private var INSTANCE: TransactionDatabase? = null
//        fun getInstance(context:Context):TransactionDatabase?{
//            synchronized(this){
//                var instance = INSTANCE
//                if (INSTANCE==null){
//                    instance = Room.databaseBuilder(
//                        context.applicationContext,
//                        TransactionDatabase::class.java,
//                        "transaction_database"
//                    )
//                        .fallbackToDestructiveMigration()
//                        .build()
//                }
//                return instance
//            }
//        }
//    }
//}