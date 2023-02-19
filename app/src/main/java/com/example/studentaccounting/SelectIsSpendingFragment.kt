package com.example.studentaccounting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.studentaccounting.databinding.FragmentSelectAmountBinding
import com.example.studentaccounting.databinding.FragmentSelectIsSpendingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SelectIsSpendingFragment : SelectFragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private lateinit var binding: FragmentSelectIsSpendingBinding

    init {
        nextPageResId = R.id.action_selectIsSpendingFragment_to_selectCategoryFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectIsSpendingBinding.inflate(inflater, container, false)

        binding.btnSpending.setOnClickListener {
            viewModel.updateIsSpending(true)
            viewModel.updateIsTransfer(false)
            gotoNextPage(binding.root)
        }

        binding.btnEarning.setOnClickListener {
            viewModel.updateIsSpending(false)
            viewModel.updateIsTransfer(false)
            gotoNextPage(binding.root)
        }

        binding.btnTransfer.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.updateSelectedCat("Transfer")
                viewModel.updateSelectedSubcat("Transfer")
                viewModel.updateIsTransfer(true)
            }
            it.findNavController().navigate(R.id.action_selectIsSpendingFragment_to_selectTransactionTypeFromFragment)
        }

        return binding.root
    }

}