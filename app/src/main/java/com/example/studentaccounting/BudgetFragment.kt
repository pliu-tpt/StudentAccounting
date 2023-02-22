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
import com.example.studentaccounting.db.entities.relations.OptionWithTotal
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
    private val filterViewModel : FilterViewModel by activityViewModels()

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
//        initAggregateRecyclerView()


        filterViewModel.filters.month.observe(viewLifecycleOwner) {
            Log.i(TransactionListFragment.MYTAG,"Observer Month Modification to : ${it.toString()}")
            if (it == "-1") {
                resetMonth()
            }
            updateFilteredTransactions()
        }

        filterViewModel.filters.year.observe(viewLifecycleOwner) {
            Log.i(TransactionListFragment.MYTAG,"Observer Year Modification to : ${it.toString()}")
            if (it == "-1") {
                resetMonth()
            }
            updateFilteredTransactions()
        }

        filterViewModel.filters.cat.observe(viewLifecycleOwner) {
            Log.i(TransactionListFragment.MYTAG,"Observer Cat Modification to : ${it.toString()}")
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

        filterViewModel.filteredTransactions.observe(viewLifecycleOwner){
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getAllFilteredAggregated(filterViewModel.filters)
                    ?.let { it1 ->
                        filterViewModel.updateOptionWithTotal(it1)
                    }
            }
        }

        viewModel.transactions.observe(viewLifecycleOwner){
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getAllFilteredWithPrefCurrency(filterViewModel.filters)
                    ?.let { it1 -> filterViewModel.updateFilteredTransactions(it1) }
            }
        }

        loadCurrentData()

        return binding.root
    }

    private fun loadCurrentData() {
        filterViewModel.filters.month.value = filterViewModel.filters.month.value
        filterViewModel.filters.year.value = filterViewModel.filters.year.value
        filterViewModel.filters.cat.value = filterViewModel.filters.cat.value
        filterViewModel.filteredTransactions.value = filterViewModel.filteredTransactions.value
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
                show(this@BudgetFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }
    }

    private fun updateFilteredTransactions() {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.getAllFilteredWithPrefCurrency(filterViewModel.filters)?.let {
                filterViewModel.updateFilteredTransactions(it)
            } ?: filterViewModel.updateFilteredTransactions(mutableListOf<TransactionWithConversion>())
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

        filterViewModel.optionWithTotal.value?.let {
            Log.i(MYTAG, "STARTUP OPTION WITH TOTAL : ${it.toString()}")
            spentData = it.filter{it1 -> it1.total > 0 }.map{ it2 -> arrayOf(it2.option, (it2.total * 100.0).roundToInt() / 100.00 ) }

            spentSeries = arrayOf(
                AASeriesElement()
                    .innerSize("85%")
                    .name("SPENT (${viewModel.preferredCurrency.value})")
                    .data(spentData.toTypedArray())
            )
        }

        filterViewModel.optionWithTotal.value?.let {
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

        updateChartsSize(chartViewSpent, spentData, chartViewEarned, earnedData)

        chartViewSpent.aa_drawChartWithChartModel(aaChartModelSpent)
        chartViewEarned.aa_drawChartWithChartModel(aaChartModelEarned)

        displayPieChart()
    }

    private fun displayPieChart(){
        filterViewModel.optionWithTotal.observe(viewLifecycleOwner){

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
                .chartType(AAChartType.Pie)
                .backgroundColor("#FFFFFFFF")
                .animationDuration(2000)
                .series(spentSeries)

            val aaChartModelEarned : AAChartModel = AAChartModel()
                .chartType(AAChartType.Pie)
                .backgroundColor("#FFFFFFFF")
                .animationDuration(2000)
                .series(earnedSeries)

            updateChartsSize(chartViewSpent, spentData, chartViewEarned, earnedData)

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
        filterLayoutBinding.tvMonth.text = "Select Month"
        filterLayoutBinding.cbMonth.isChecked = true
    }

    private fun resetCat() {
        filterLayoutBinding.actvCategory.text = null
        filterLayoutBinding.cbCat.isChecked = true
        disableEditText(filterLayoutBinding.actvCategory)
    }

    private fun optionListItemClicked(option:OptionWithTotal){
        if (filterViewModel.filters.cat.value == "-1"){
            filterViewModel.updateCat(option.option)
            filterLayoutBinding.cbCat.isChecked = false
        }
    }

//    private fun displayAggregateOptionList(){
//        filterViewModel.filteredTransactions.observe(viewLifecycleOwner){
//            CoroutineScope(Dispatchers.Main).launch {
//                viewModel.getAllFilteredAggregated(filterViewModel.filters)
//                    ?.let { it1 ->
//                        aggAdapter.setList(it1)
//                        aggAdapter.addTotal()
//                        aggAdapter.notifyDataSetChanged()
//                    }
//            }
//        }
//    }

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
                filterViewModel.updateCat(item)
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