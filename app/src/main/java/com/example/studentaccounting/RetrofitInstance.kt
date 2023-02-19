package com.example.studentaccounting
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance {

    companion object {
        private val interceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        private val client = OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS) // time to establish conn.
                .readTimeout(20, TimeUnit.SECONDS) // max gap between two data packets when waiting for the server's response
                .writeTimeout(25, TimeUnit.SECONDS) // max gap between two data packets when sending them for the server
        }.build()

        val BASE_URL = "https://jsonplaceholder.typicode.com" // Google Drive ?

        fun getRetrofitInstance():Retrofit{
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build()
        }

    }

}