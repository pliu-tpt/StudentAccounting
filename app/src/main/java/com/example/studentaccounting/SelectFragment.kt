package com.example.studentaccounting

import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlin.properties.Delegates

// Fragment to Select something, always have a next page/Fragment
open class SelectFragment(): Fragment() {

    var nextPageResId by Delegates.notNull<Int>()

    fun gotoNextPage(view : View){
        view.findNavController().navigate(nextPageResId)
    }

}