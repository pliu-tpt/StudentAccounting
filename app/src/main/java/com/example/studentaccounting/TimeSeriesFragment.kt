package com.example.studentaccounting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.studentaccounting.databinding.FragmentBudgetBinding
import com.example.studentaccounting.databinding.FragmentTimeSeriesBinding

class TimeSeriesFragment : Fragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private val timeSeriesViewModel : TimeSeriesViewModel by activityViewModels()

    private lateinit var binding: FragmentTimeSeriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimeSeriesBinding.inflate(inflater, container, false)

        return binding.root
    }

}