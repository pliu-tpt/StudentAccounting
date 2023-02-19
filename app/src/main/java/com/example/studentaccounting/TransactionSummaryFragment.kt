package com.example.studentaccounting

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.studentaccounting.databinding.FragmentTransactionSummaryBinding


class TransactionSummaryFragment : SelectFragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private lateinit var binding: FragmentTransactionSummaryBinding

    private var amountString: String = "Amount: "

    init {
        nextPageResId = R.id.action_transactionSummaryFragment_to_transactionListFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionSummaryBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        viewModel.selectedCat.observe(viewLifecycleOwner) {
            binding.tvCategory.text = "Category: $it"
        }

        viewModel.selectedSubcat.observe(viewLifecycleOwner) {
            binding.tvSubcat.text = "Sub-Category: $it"
        }

        viewModel.selectedTypeTo.observe(viewLifecycleOwner) {
            var selectedFrom = viewModel.selectedTypeFrom.value
            binding.tvTransactionType.text = "From $selectedFrom To $it"
        }

        viewModel.selectedType.observe(viewLifecycleOwner) {
            binding.tvTransactionType.text = "Type: $it"
        }

        viewModel.selectedCurrency.observe(viewLifecycleOwner) {
            binding.tvCurrency.text = "Currency: $it"
        }

        viewModel.selectedIsSpending.observe(viewLifecycleOwner) {
            amountString = if (it) "Amount: -" else "Amount: +"
            binding.tvTransactionAmount.setTextColor(
                ContextCompat.getColor(
                    requireContext(),if (it) R.color.spending else R.color.earning
                )
            )
        }

        viewModel.selectedAmount.observe(viewLifecycleOwner) {
            binding.tvTransactionAmount.text = amountString + it
        }

        viewModel.selectedName.observe(viewLifecycleOwner) {
            binding.tvNameCard.text = "Name: $it"
        }

        binding.btnConfirm.setOnClickListener {
            viewModel.insertSelectedTransaction()
            gotoNextPage(it)
        }

        return binding.root
    }

}