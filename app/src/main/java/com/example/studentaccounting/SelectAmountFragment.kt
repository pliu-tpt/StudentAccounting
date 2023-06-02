package com.example.studentaccounting

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.example.studentaccounting.databinding.FragmentSelectAmountBinding


class SelectAmountFragment : SelectFragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private lateinit var binding: FragmentSelectAmountBinding

//    private lateinit var spinner : Spinner
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var currentCurrency : String

    init {
        nextPageResId = R.id.action_selectAmountFragment_to_transactionSummaryFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onResume() {
        super.onResume()
//        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectAmountBinding.inflate(inflater, container, false)

        setupAutoCompleteTextView()

        viewModel.selectedAmount.observe(viewLifecycleOwner) {
            if (it == 0.0){
                binding.etAmount.setText("")
            } else {
                binding.etAmount.setText(it.toString())
            }
        }

        viewModel.selectedCurrency.observe(viewLifecycleOwner) {
            binding.actvCurrency.setText(it.toString(), false)
        }

        viewModel.preferredCurrency.observe(viewLifecycleOwner) {
            binding.actvCurrency.setText(it.toString(), false)
        }

        viewModel.selectedName.observe(viewLifecycleOwner) {
            binding.etName.setText(it.toString())
        }

        binding.btnNext.setOnClickListener {

            if (!TextUtils.isEmpty(binding.etName.text.toString())){
                viewModel.updateSelectedName(binding.etName.text.toString())

                if (binding.actvCurrency.text.toString() != "") {
                    val selectedOption = binding.actvCurrency.text.toString()
                    viewModel.updateSelectedCurrency(selectedOption)

                    gotoNextPage(it)
                    viewModel.updateSelectedAmount((binding.etAmount.text.toString()).toDouble())
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Please enter a currency",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                Toast.makeText(
                    requireActivity(),
                    "Please enter a title to your transaction",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    private fun displayCurrencyList(){
        viewModel.currencies.observe(viewLifecycleOwner) {
            adapter.clear()
            adapter.addAll(it)

            currentCurrency = viewModel.selectedCurrency.value?.let{
                viewModel.selectedCurrency.value
            } ?: run {
                viewModel.preferredCurrency.value.toString()
            }
            binding.actvCurrency.setText(currentCurrency, false)
            adapter.notifyDataSetChanged()
        }
    }


    private fun setupAutoCompleteTextView(){

        autoCompleteTextView = binding.actvCurrency
        adapter = ArrayAdapter(requireContext(),
            R.layout.spinner_currency_item,
            mutableListOf<String>())
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                var item = parent?.getItemAtPosition(id.toInt()).toString()
                viewModel.updateSelectedCurrency(item)
//                Toast.makeText(requireContext(), "$item is Clicked", Toast.LENGTH_LONG).show()
            }
        }

        displayCurrencyList()
    }
//

}


//    private fun setupSpinner(){
//
//        spinner = binding.spCurrency
//        adapter = ArrayAdapter(requireContext(),
//            R.layout.spinner_currency_item,
//            mutableListOf<String>(getString(R.string.other)))
//        spinner.adapter = adapter
//
//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                if (adapter.getItem(position) == getString(R.string.other)){
//                    binding.etCurrency.isVisible = true
//                } else {
//                    viewModel.updateSelectedCurrency(adapter.getItem(position).toString())
//                    binding.etCurrency.isVisible = false
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                binding.etCurrency.isVisible = false
//            }
//        }
//
//    }
//
//    private fun setSpinnerPositionTo(string: String) {
//        // set the spinner to the value string if it exists
//        var currencyPos = adapter.getPosition(string)
//        if (currencyPos >= 0) {
//            spinner.setSelection(currencyPos)
//            binding.etCurrency.isVisible = false
//        } else { // when the preferred Currency is not yet in the DB
//            binding.etCurrency.isVisible = true
//            binding.etCurrency.setText(string)
//        }
//    }