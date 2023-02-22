package com.example.studentaccounting.db

import androidx.lifecycle.MutableLiveData

data class Filters(
//    val query: String? = null,
    var month: MutableLiveData<String?> = MutableLiveData<String?>(),
    var year: MutableLiveData<String?> = MutableLiveData<String?>(),
    var cat: MutableLiveData<String?> = MutableLiveData<String?>(),
    var isSorted: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(),
    var prefCurrency: MutableLiveData<String> = MutableLiveData<String>(),
    // all the other filters
    var startMonth: MutableLiveData<String?> = MutableLiveData<String?>(),
    var endMonth: MutableLiveData<String?> = MutableLiveData<String?>(),
)
