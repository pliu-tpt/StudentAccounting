package com.example.studentaccounting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.studentaccounting.TransactionListFragment.Companion.MYTAG
import com.example.studentaccounting.databinding.CommonFilterLayoutBinding
import com.example.studentaccounting.databinding.FragmentBudgetBinding
import com.example.studentaccounting.db.entities.relations.TransactionWithConversion
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

class BudgetFragment : Fragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private val budgetViewModel : BudgetViewModel by activityViewModels()

    private lateinit var binding: FragmentBudgetBinding
    private lateinit var filterLayoutBinding: CommonFilterLayoutBinding

    private lateinit var chartViewSpent : AAChartView
    private lateinit var chartViewEarned : AAChartView

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var adapterCategory: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBudgetBinding.inflate(inflater, container, false)
        filterLayoutBinding = CommonFilterLayoutBinding.bind(binding.root)

        updateFilteredTransactions()
        setupAutoCompleteTextView()
        setupPieChart()

        budgetViewModel.filters.month.observe(viewLifecycleOwner) {
            Log.i(MYTAG,"Observer Month Modification to : ${it.toString()}")
            if (it == "-1") {
                resetMonth()
            }
            updateFilteredTransactions()
        }

        budgetViewModel.filters.year.observe(viewLifecycleOwner) {
            Log.i(MYTAG,"Observer Year Modification to : ${it.toString()}")
            if (it == "-1") {
                resetMonth()
            }
            updateFilteredTransactions()
        }

        budgetViewModel.filters.cat.observe(viewLifecycleOwner) {
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
                    budgetViewModel.updateCat("-1")
                }
                false -> {enableEditText(filterLayoutBinding.actvCategory)}
            }
        }

        filterLayoutBinding.cbMonth.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    budgetViewModel.updateMonthAndYear(-1, -1)
                }
                false -> {}
            }
        }

//        viewModel.transactions.observe(viewLifecycleOwner){
//            CoroutineScope(Dispatchers.Main).launch {
//                viewModel.getAllFilteredWithPrefCurrency(budgetViewModel.filters)
//                    ?.let { it1 -> budgetViewModel.updateFilteredTransactions(it1) }
//            }
//        }
//
        budgetViewModel.filteredTransactions.observe(viewLifecycleOwner){
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getAllFilteredAggregated(budgetViewModel.filters)
                    ?.let { it1 ->
                        budgetViewModel.updateOptionWithTotal(it1)
                    }
            }
        }

//        loadCurrentData()

        return binding.root
    }

    private fun loadCurrentData() {
        budgetViewModel.filters.month.value = budgetViewModel.filters.month.value
        budgetViewModel.filters.year.value = budgetViewModel.filters.year.value
        budgetViewModel.filters.cat.value = budgetViewModel.filters.cat.value
//        filterViewModel.filteredTransactions.value = filterViewModel.filteredTransactions.value
    }

    private fun popupMonthDialog() {
        if (!filterLayoutBinding.cbMonth.isChecked) {
            MonthYearPickerDialog(budgetViewModel.date).apply {
                setListener { _, year, month, dayOfMonth ->
                    Toast.makeText(
                        requireContext(),
                        "Set date: $year/$month/$dayOfMonth",
                        Toast.LENGTH_LONG
                    ).show()
                    budgetViewModel.updateMonthAndYear(month + 1, year)
                    budgetViewModel.updateDate(Date(year + 1, month, dayOfMonth))
                    filterLayoutBinding.tvMonth.text = "${String.format("%02d", month + 1)}-$year"
                    filterLayoutBinding.cbMonth.isChecked = false
                }
                show(this@BudgetFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }
    }

    private fun updateFilteredTransactions() {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.getAllFilteredWithPrefCurrency(budgetViewModel.filters)?.let {
                budgetViewModel.updateFilteredTransactions(it)
            } ?: budgetViewModel.updateFilteredTransactions(mutableListOf<TransactionWithConversion>())
        }

    }

    private fun setupPieChart(){
        chartViewSpent = binding.aaChartViewSpent
        chartViewEarned = binding.aaChartViewEarned

        var spentSeries : Array<Any> = arrayOf(AASeriesElement()
            .innerSize("85%")
            .name("ERROR")
            .data(arrayOf()))

        var spentData : List<Any> = listOf()

        var earnedSeries : Array<Any> = arrayOf(AASeriesElement()
            .innerSize("85%")
            .name("ERROR")
            .data(arrayOf()))

        var earnedData : List<Any> = listOf()

        budgetViewModel.optionWithTotal.value?.let {
            Log.i(MYTAG, "STARTUP OPTION WITH TOTAL : ${it.toString()}")
            spentData = it.filter{it1 -> it1.total > 0 }.map{ it2 -> arrayOf(it2.option, (it2.total * 100.0).roundToInt() / 100.00 ) }

            spentSeries = arrayOf(
                AASeriesElement()
                    .innerSize("85%")
                    .name("SPENT (${viewModel.preferredCurrency.value})")
                    .data(spentData.toTypedArray())
            )
        }

        budgetViewModel.optionWithTotal.value?.let {
            earnedData = it.filter {it1 -> it1.total < 0 }.map { it2 -> arrayOf(it2.option, ( - it2.total * 100.0).roundToInt() / 100.00 ) }
            earnedSeries = arrayOf(
                AASeriesElement()
                    .innerSize("85%")
                    .name("EARNED (${viewModel.preferredCurrency.value})")
                    .data(earnedData.toTypedArray())
            )
        }
        if (earnedData.isEmpty()) {
            Log.i(MYTAG, "EARNED S is EMPTY ")
        } else {
            Log.i(MYTAG, earnedData.toString())
        }

        val aaChartModelSpent : AAChartModel = AAChartModel()
            .chartType(AAChartType.Pie)
            .backgroundColor("#FFFFFFFF")
            .animationDuration(2000)
            .series(spentSeries)

        val aaChartModelEarned : AAChartModel = AAChartModel()
            .chartType(AAChartType.Pie)
            .backgroundColor("#FFFFFFFF")
            .animationDuration(2000)
            .series(earnedSeries)

//        updateChartsSize(chartViewSpent, spentData, chartViewEarned, earnedData)

        chartViewSpent.aa_drawChartWithChartModel(aaChartModelSpent)
        chartViewEarned.aa_drawChartWithChartModel(aaChartModelEarned)

        displayPieChart()
    }

    private fun displayPieChart(){
        budgetViewModel.optionWithTotal.observe(viewLifecycleOwner){

            var spentSeries : Array<Any> = arrayOf(AASeriesElement()
                .innerSize("85%")
                .name("ERROR")
                .data(arrayOf()))

            var spentData : List<Any> = listOf()

            var earnedSeries : Array<Any> = arrayOf(AASeriesElement()
                .innerSize("85%")
                .name("ERROR")
                .data(arrayOf()))

            var earnedData : List<Any> = listOf()

            it?.let {
                spentData = it.filter{it1 -> it1.total > 0 }.map{ it2 -> arrayOf(it2.option, (it2.total * 100.0).roundToInt() / 100.00 ) }

                spentSeries = arrayOf(
                    AASeriesElement()
                        .innerSize("85%")
                        .name("SPENT (${viewModel.preferredCurrency.value})")
                        .data(spentData.toTypedArray())
                )
            }

            it?.let {
                earnedData = it.filter {it1 -> it1.total < 0 }.map { it2 -> arrayOf(it2.option, ( - it2.total * 100.0).roundToInt() / 100.00 ) }
                earnedSeries = arrayOf(
                    AASeriesElement()
                        .innerSize("85%")
                        .name("EARNED (${viewModel.preferredCurrency.value})")
                        .data(earnedData.toTypedArray())
                )
            }
            if (earnedData.isEmpty()) {
                Log.i(MYTAG, "EARNED S is EMPTY ")
            } else {
                Log.i(MYTAG, earnedData.toString())
            }

            val aaChartModelSpent : AAChartModel = AAChartModel()
                .title("Your Spendings")
                .chartType(AAChartType.Pie)
                .colorsTheme(arrayOf("#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2",
                    "#7f7f7f", "#bcbd22", "#17becf"))
                .backgroundColor("#FFFFFFFF")
                .animationDuration(2000)
                .series(spentSeries)

            val aaChartModelEarned : AAChartModel = AAChartModel()
                .title("Your Earnings")
                .colorsTheme(arrayOf("#0ead69", "#3bceac", "#dde7c7","#2c6e49","#62b6cb",
                    "#7fb800","#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2",
                    "#7f7f7f", "#bcbd22", "#17becf"))
                .chartType(AAChartType.Pie)
                .backgroundColor("#FFFFFFFF")
                .animationDuration(2000)
                .series(earnedSeries)

//            updateChartsSize(chartViewSpent, spentData, chartViewEarned, earnedData)

            chartViewSpent.aa_refreshChartWithChartModel(aaChartModelSpent)
            chartViewEarned.aa_refreshChartWithChartModel(aaChartModelEarned)
        }
    }

    private fun updateChartsSize(chartViewS: AAChartView, chartSeriesS:List<Any>,
                                 chartViewE: AAChartView, chartSeriesE:List<Any>) {
        if (chartSeriesS.isEmpty()){
            if (chartSeriesE.isEmpty()){
                chartViewE.visibility = GONE
                chartViewS.visibility = GONE
            } else {
                chartViewS.visibility = GONE
                chartViewE.visibility = VISIBLE
            }
        } else {
            if (chartSeriesE.isEmpty()){
                chartViewE.visibility = GONE
                chartViewS.visibility = VISIBLE
            } else {
                chartViewS.visibility = VISIBLE
                chartViewE.visibility = VISIBLE
            }
        }
    }

    private fun resetMonth() {
        filterLayoutBinding.tvMonth.text = getString(R.string.select_month)
        filterLayoutBinding.cbMonth.isChecked = true
    }

    private fun resetCat() {
        filterLayoutBinding.actvCategory.text = null
        filterLayoutBinding.cbCat.isChecked = true
        disableEditText(filterLayoutBinding.actvCategory)
    }

    private fun displayCategoriesList(){
        viewModel.categories.observe(viewLifecycleOwner) {
            adapterCategory.clear()
            adapterCategory.addAll(it)
            adapterCategory.notifyDataSetChanged()
        }
    }

    private fun setupAutoCompleteTextView(){
        autoCompleteTextView = filterLayoutBinding.actvCategory
        adapterCategory = ArrayAdapter(requireContext(),
            R.layout.spinner_currency_item,
            mutableListOf<String>())
        viewModel.categories.value?.let { adapterCategory.addAll(it) }
        autoCompleteTextView.setAdapter(adapterCategory)
        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, _, id ->
                var item = parent?.getItemAtPosition(id.toInt()).toString()
                budgetViewModel.updateCat(item)
                filterLayoutBinding.cbCat.isChecked = false
            }

//        autoCompleteTextView.setText(filterViewModel.filters.cat.value)

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