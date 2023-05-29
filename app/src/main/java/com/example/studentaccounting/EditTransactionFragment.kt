package com.example.studentaccounting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.studentaccounting.databinding.FragmentEditTransactionBinding
import com.example.studentaccounting.db.entities.Transaction


class EditTransactionFragment : Fragment() {

    private val viewModel : TransactionViewModel by activityViewModels()

    private lateinit var binding: FragmentEditTransactionBinding

    private lateinit var tvCurrency: AutoCompleteTextView
    private lateinit var tvCategory: AutoCompleteTextView
    private lateinit var tvSubcat: AutoCompleteTextView
    private lateinit var tvType: AutoCompleteTextView

    private lateinit var adapterCurrency: ArrayAdapter<String>
    private lateinit var adapterCategory: ArrayAdapter<String>
    private lateinit var adapterSubcat: ArrayAdapter<String>
    private lateinit var adapterType: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEditTransactionBinding.inflate(inflater, container, false)


        setupTVCurrency()
        setupTVCategory()
        setupTVSubcat()
        setupTVType()

        viewModel.transactionToEdit.observe(viewLifecycleOwner) {
            with (binding) {
                actvCurrency.setText(it.currency)
                actvCategory.setText(it.category)
                actvSubcat.setText(it.subcategory)
                actvType.setText(it.type)
                etName2.setText(it.name)
                etDate.setText(it.date)
                etAmount.setText(it.amount.toString())
                if (it.isSpending) {
                    rbSpending.isChecked = true
                } else {
                    rbEarning.isChecked = true
                }
                btnEdit.setOnClickListener {it1 ->
                    viewModel.editTransaction(
                        Transaction(
                            it.id,
                            etName2.text.toString(),
                            actvCategory.text.toString(),
                            actvSubcat.text.toString(),
                            actvType.text.toString(),
                            etAmount.text.toString().toDouble(),
                            actvCurrency.text.toString(),
                            rbSpending.isChecked,
                            etDate.text.toString()
                        )
                    )
                    it1.findNavController().popBackStack()
                }
            }
        }
        return binding.root
    }

    private fun setupTVCurrency(){

        tvCurrency = binding.actvCurrency
        adapterCurrency = ArrayAdapter(requireContext(),
            R.layout.spinner_currency_item,
            mutableListOf<String>())
        tvCurrency.setAdapter(adapterCurrency)

        tvCurrency.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = parent?.getItemAtPosition(id.toInt()).toString()
                tvCurrency.setText(item)
            }
        }

        displayCurrencyList()
    }

    private fun displayCurrencyList(){
        viewModel.currencies.observe(viewLifecycleOwner) {
            adapterCurrency.clear()
            adapterCurrency.addAll(it)
        }
    }

    private fun setupTVCategory(){

        tvCategory = binding.actvCategory
        adapterCategory = ArrayAdapter(requireContext(),
            R.layout.spinner_currency_item,
            mutableListOf<String>())
        tvCategory.setAdapter(adapterCategory)

        tvCategory.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = parent?.getItemAtPosition(id.toInt()).toString()
                tvCategory.setText(item)
//                Toast.makeText(requireContext(), "$item is Clicked", Toast.LENGTH_LONG).show()
            }
        }

        displayCategoryList()
    }

    private fun displayCategoryList(){
        viewModel.categories.observe(viewLifecycleOwner) {
            adapterCategory.clear()
            adapterCategory.addAll(it)
        }
    }

    private fun setupTVSubcat(){

        tvSubcat = binding.actvSubcat
        adapterSubcat = ArrayAdapter(requireContext(),
            R.layout.spinner_currency_item,
            mutableListOf<String>())
        tvSubcat.setAdapter(adapterSubcat)

        tvSubcat.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = parent?.getItemAtPosition(id.toInt()).toString()
                tvSubcat.setText(item)
//                Toast.makeText(requireContext(), "$item is Clicked", Toast.LENGTH_LONG).show()
            }
        }

        displaySubcatList()
    }

    private fun displaySubcatList(){
        viewModel.subcategories.observe(viewLifecycleOwner) {
            adapterSubcat.clear()
            adapterSubcat.addAll(it)
        }
    }

    private fun setupTVType(){

        tvType = binding.actvType
        adapterType = ArrayAdapter(requireContext(),
            R.layout.spinner_currency_item,
            mutableListOf<String>())
        tvType.setAdapter(adapterType)

        tvType.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = parent?.getItemAtPosition(id.toInt()).toString()
                tvType.setText(item)
//                Toast.makeText(requireContext(), "$item is Clicked", Toast.LENGTH_LONG).show()
            }
        }

        displayTypeList()
    }

    private fun displayTypeList(){
        viewModel.transactionTypes.observe(viewLifecycleOwner) {
            adapterType.clear()
            adapterType.addAll(it)
        }
    }


}