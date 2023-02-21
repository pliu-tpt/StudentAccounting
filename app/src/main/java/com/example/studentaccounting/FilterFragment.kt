package com.example.studentaccounting

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.FOCUSABLE
import android.view.View.NOT_FOCUSABLE
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentaccounting.TransactionListFragment.Companion.MYTAG
import com.example.studentaccounting.databinding.CommonFilterLayoutBinding
import com.example.studentaccounting.databinding.FragmentFilterBinding
import com.example.studentaccounting.db.entities.relations.OptionWithTotal
import com.example.studentaccounting.db.entities.relations.TransactionWithConversion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FilterFragment : Fragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private val filterViewModel : FilterViewModel by activityViewModels()

    private lateinit var binding: FragmentFilterBinding
    private lateinit var filterLayoutBinding: CommonFilterLayoutBinding

    private lateinit var filteredAdapter: TransactionRecyclerViewAdapter
    private lateinit var aggAdapter: AggregateRecyclerViewAdapter

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var adapterCategory: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFilterBinding.inflate(inflater, container, false)

        updateFilteredTransactions()
        setupAutoCompleteTextView()
        initFilteredRecyclerView()
        initAggregateRecyclerView()


        filterViewModel.filters.month.observe(viewLifecycleOwner) {
            Log.i(MYTAG,"Observer Month Modification to : ${it.toString()}")
            if (it == "-1") {
                resetMonth()
            }
            updateFilteredTransactions()
        }

        filterViewModel.filters.year.observe(viewLifecycleOwner) {
            Log.i(MYTAG,"Observer Year Modification to : ${it.toString()}")
            if (it == "-1") {
                resetMonth()
            }
            updateFilteredTransactions()
        }

        filterViewModel.filters.cat.observe(viewLifecycleOwner) {
            Log.i(MYTAG,"Observer Cat Modification to : ${it.toString()}")
            if (it == "-1") {
                resetCat()
            } else {
                autoCompleteTextView.setText(it)
            }
            updateFilteredTransactions()
        }

        filterLayoutBinding.tvMonth.setOnClickListener {
            popupMonthDialog()
        }

        filterLayoutBinding.cbCat.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    filterViewModel.updateCat("-1")
                }
                false -> {enableEditText(filterLayoutBinding.actvCategory)}
            }
        }

        filterLayoutBinding.cbMonth.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    filterViewModel.updateMonthAndYear(-1, -1)
                }
                false -> {}
            }
        }



        return binding.root
    }

    private fun popupMonthDialog() {
        if (!filterLayoutBinding.cbMonth.isChecked) {
            MonthYearPickerDialog(filterViewModel.date).apply {
                setListener { _, year, month, dayOfMonth ->
                    Toast.makeText(
                        requireContext(),
                        "Set date: $year/$month/$dayOfMonth",
                        Toast.LENGTH_LONG
                    ).show()
                    filterViewModel.updateMonthAndYear(month + 1, year)
                    filterViewModel.updateDate(Date(year + 1, month, dayOfMonth))
                    filterLayoutBinding.tvMonth.text = "${String.format("%02d", month + 1)}-$year"
                    filterLayoutBinding.cbMonth.isChecked = false
                }
                show(this@FilterFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }
    }

    private fun updateFilteredTransactions() {
        CoroutineScope(Dispatchers.Main).launch {
//            Log.i(MYTAG, "CALL TO MODIF ${viewModel.getAllFiltered(filterViewModel.filters).toString()}")
//            Log.i(MYTAG, "CALL TO AGG ${viewModel.getAllFilteredAggregated(filterViewModel.filters).toString()}")
            viewModel.getAllFilteredWithPrefCurrency(filterViewModel.filters)?.let {
                filterViewModel.updateFilteredTransactions(it)
            } ?: filterViewModel.updateFilteredTransactions(mutableListOf<TransactionWithConversion>())
        }
    }

    private fun resetMonth() {
        filterLayoutBinding.tvMonth.text = "Select Month"
        filterLayoutBinding.cbMonth.isChecked = true
    }

    private fun resetCat() {
        filterLayoutBinding.actvCategory.text = null
        filterLayoutBinding.cbCat.isChecked = true
        disableEditText(filterLayoutBinding.actvCategory)
    }

    private fun initFilteredRecyclerView(){

        binding.rvFilteredTransactions.layoutManager = LinearLayoutManager(requireContext())
        filteredAdapter = TransactionRecyclerViewAdapter{
                selectedTransaction, view -> transactionListItemLongClicked(selectedTransaction, view)
        }

        binding.rvFilteredTransactions.adapter = filteredAdapter

        displayTransactionTypesList()
    }

    private fun initAggregateRecyclerView(){
        // lots of shenanigans to have modular height (horizontal in sflow): https://stackoverflow.com/questions/57890199/how-to-set-recycler-height-to-highest-item-in-recyclerview

        binding.rvFilteredAggregate.layoutManager = LinearLayoutManager(requireContext())

        aggAdapter =
            AggregateRecyclerViewAdapter(viewModel.preferredCurrency.value!!) { optionWithTotal ->
                optionListItemClicked(optionWithTotal)
            }

//        binding.rvFilteredAggregate.visibility = View.INVISIBLE
        binding.rvFilteredAggregate.adapter = aggAdapter

//        binding.rvFilteredAggregate.setHasFixedSize(true)

        displayAggregateOptionList()
    }

    private fun optionListItemClicked(option:OptionWithTotal){
        if (filterViewModel.filters.cat.value == "-1"){
            filterViewModel.updateCat(option.option)
            filterLayoutBinding.cbCat.isChecked = false
        }
    }

    private fun displayAggregateOptionList(){
        filterViewModel.filteredTransactions.observe(viewLifecycleOwner){
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getAllFilteredAggregated(filterViewModel.filters)
                    ?.let { it1 ->
                        filterViewModel.updateOptionWithTotal(it1)
                        aggAdapter.setList(it1)
                        Log.i(MYTAG, "TEST ID ${binding.rvFilteredAggregate.layoutManager?.findViewByPosition(it1.size + 1)?.id.toString()}")
                        aggAdapter.addTotal()
                        aggAdapter.notifyDataSetChanged()
                    }
            }
        }
    }

    private fun displayTransactionTypesList(){
        // when the database is modified, it updates the filtered list and displays the update
        viewModel.transactions.observe(viewLifecycleOwner){
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getAllFilteredWithPrefCurrency(filterViewModel.filters)
                    ?.let { it1 -> filterViewModel.updateFilteredTransactions(it1) }
                filterViewModel.filteredTransactions.value?.let { it1 -> filteredAdapter.setList(it1) }
                filteredAdapter.notifyDataSetChanged()
            }
        }

        filterViewModel.filteredTransactions.observe(viewLifecycleOwner){
            filteredAdapter.setList(it)
            filteredAdapter.notifyDataSetChanged()
        }
    }

//    fun showAllOption(textView: TextView) {
//        textView.text = "All"
//        // update the FilterViewModel with the all option
//        filterViewModel.updateMonthAndYear((-1).toString(), -1)
//    }

    private fun transactionListItemLongClicked(t: TransactionWithConversion, view: View){
        val transaction = t.transaction
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.popup_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    // Handle Edit option
                    Log.i("MYTAG", "Edit transaction number ${transaction.id}")
                    true
                }
                R.id.delete -> {
                    // Handle Delete option
                    Log.i("MYTAG", "Delete transaction number ${transaction.id}")
                    viewModel.deleteTransaction(transaction)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun displayCategoriesList(){
        viewModel.categories.observe(viewLifecycleOwner) {
            adapterCategory.clear()
            adapterCategory.addAll(it)
            adapterCategory.notifyDataSetChanged()
        }
    }

    fun setupAutoCompleteTextView(){
        autoCompleteTextView = filterLayoutBinding.actvCategory
        adapterCategory = ArrayAdapter(requireContext(),
            R.layout.spinner_currency_item,
            mutableListOf<String>())
        viewModel.categories.value?.let { adapterCategory.addAll(it) }
        autoCompleteTextView.setAdapter(adapterCategory)
        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, _, id ->
                var item = parent?.getItemAtPosition(id.toInt()).toString()
                filterViewModel.updateCat(item)
                filterLayoutBinding.cbCat.isChecked = false
            }

        autoCompleteTextView.setText(filterViewModel.filters.cat.value)

        displayCategoriesList()
    }

    private fun disableEditText(editText: EditText){
        editText.focusable = NOT_FOCUSABLE
        editText.isFocusableInTouchMode = false
    }

    private fun enableEditText(editText: EditText){
        editText.focusable = FOCUSABLE
        editText.isFocusableInTouchMode = true
    }
}