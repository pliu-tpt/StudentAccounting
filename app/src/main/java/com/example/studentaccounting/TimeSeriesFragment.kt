package com.example.studentaccounting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.studentaccounting.TransactionListFragment.Companion.MYTAG
import com.example.studentaccounting.databinding.FragmentTimeSeriesBinding
import com.example.studentaccounting.db.entities.relations.OptionWithDateAndTotal
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class TimeSeriesFragment : Fragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private val tsViewModel : TimeSeriesViewModel by activityViewModels()

    private lateinit var binding: FragmentTimeSeriesBinding

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var adapterCategory: ArrayAdapter<String>

    private lateinit var chartViewLineGraph : AAChartView
//    private lateinit var chartViewTypeGraph : AAChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimeSeriesBinding.inflate(inflater, container, false)

        updateGraphTransactions()
        setupAutoCompleteTextView()

        tsViewModel.filters.startMonth.observe(viewLifecycleOwner) {
            if (it == "-1"){
                 if (tsViewModel.filters.endMonth.value == "-1") {
                     resetMonths()
                 }
            } else {
                binding.tvStartMonth.text = it
                binding.cbMonth.isChecked = false
            }
            updateGraphTransactions()
        }

        tsViewModel.filters.endMonth.observe(viewLifecycleOwner) {
            if (it == "-1"){
                if (tsViewModel.filters.startMonth.value == "-1") {
                    resetMonths()
                }
            } else {
                binding.tvEndMonth.text = it
                binding.cbMonth.isChecked = false
            }
            updateGraphTransactions()
        }

        tsViewModel.filters.cat.observe(viewLifecycleOwner) {
            Log.i(TransactionListFragment.MYTAG,"Observer Cat Modification to : ${it.toString()}")
            if (it == "-1") {
                resetCat()
            } else {
                autoCompleteTextView.setText(it)
            }
            updateGraphTransactions()
        }

        binding.tvStartMonth.setOnClickListener {
            popupStartMonthDialog()
        }

        binding.tvEndMonth.setOnClickListener {
            popupEndMonthDialog()
        }

        binding.cbCat.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    tsViewModel.updateCat("-1")
                }
                false -> {enableEditText(binding.actvCategory)}
            }
        }

        binding.cbMonth.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    tsViewModel.updateStartMonthAndYear("-1")
                    tsViewModel.updateEndMonthAndYear("-1")
                }
                false -> {}
            }
        }

        viewModel.transactions.observe(viewLifecycleOwner){
            updateGraphTransactions()
        }

//        tsViewModel.updateCat("-1")
//        tsViewModel.updateStartMonthAndYear("-1")
//        tsViewModel.updateEndMonthAndYear("-1")

        loadCurrentData()
        setupLineGraph()

        return binding.root
    }

    private fun loadCurrentData() {
        tsViewModel.filters.startMonth.value = tsViewModel.filters.startMonth.value
        tsViewModel.filters.endMonth.value = tsViewModel.filters.endMonth.value
        tsViewModel.filters.cat.value = tsViewModel.filters.cat.value
//        filterViewModel.lineGraphAggregated.value = filterViewModel.lineGraphAggregated.value
    }

    private fun getMonthYearPairs(startMonthYear: String, endMonthYear: String): List<String> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val startMonth = YearMonth.parse(startMonthYear, formatter)
        val endMonth = YearMonth.parse(endMonthYear, formatter)

        val monthYearPairs = mutableListOf<String>()
        var currentMonth = startMonth
        while (currentMonth.isBefore(endMonth.plusMonths(1))) {
            monthYearPairs.add(currentMonth.format(formatter))
            currentMonth = currentMonth.plusMonths(1)
        }

        return monthYearPairs
    }

    private fun buildAACharModelFrom(list: List<OptionWithDateAndTotal>?): AAChartModel {
        Log.i(MYTAG, "Before generating the chart model, the lineGraphAggregated is ${tsViewModel.lineGraph.value}")
        val defaultValue = AAChartModel()
            .chartType(AAChartType.Line)
            .title("Your Spendings Over Time")
            .yAxisTitle("Spendings (${viewModel.preferredCurrency.value})")
            .dataLabelsEnabled(true)
            .categories(arrayOf("Nothing"))
            .series(arrayOf(
                AASeriesElement()
                    .name("Nothing To Display")
                    .data(arrayOf())))
        if (list.isNullOrEmpty()) {
            Log.i(MYTAG, "BUILD AACHART RETURN BEC NULL")
            return defaultValue
        }

        var allSeries : MutableList<Any> = mutableListOf<Any>() // contains a list of AASeriesElement

        val allOptions = list.map { it -> it.option }.distinct()
        val allDates = list.map { it -> it.month }.distinct() // all distincts "year-month" pairs
        val startMonth = allDates.min().split("-")
        val endMonth = allDates.max().split("-")

        val startM:Int = startMonth[1].toInt()
        val startY:Int = startMonth[0].toInt()

        val endM:Int = endMonth[1].toInt()
        val endY:Int = endMonth[0].toInt()

        val allCategoriesDateSize = endM-startM+1 + (endY-startY)*12

//        println("Number of months:$allCategoriesDateSize")

//        var allCategoriesDate = List<String>(allCategoriesDateSize, init = {
//                index -> "${startY + index / 12}-${String.format("%02d", 1+(startM - 1 + index % 12)%12)}"
//        }) // (e.g. [2022-7, 2022-8])

        var allCategoriesDate = getMonthYearPairs(allDates.min(), allDates.max())

        Log.i("TS", "$startM-$startY: $endM-$endY")
        Log.i("TS", allCategoriesDate.toString())

        for (option in allOptions) { // iterate through all options, (e.g. Voyage, Bouffe)
            Log.i(MYTAG, option)
            var optionSeries = AASeriesElement().name(option)

            var optionData = Array<Any>(allCategoriesDateSize) {0.0} // a list of amounts with default value 0

            val filtered = list.filter { it.option == option } // only the values corresponding in option
            val allOptionDates = filtered.map { it.month }
            val allOptionAmount = filtered.map { it.total }

            Log.i(MYTAG, allCategoriesDate.toList().toString())
            Log.i(MYTAG, allOptionDates.toList().toString())
            Log.i(MYTAG, allOptionAmount.toList().toString())

            for ((i, month) in allCategoriesDate.withIndex()) { // run through all the possible months
                if (month in allOptionDates) { // if the month is present
                    optionData[i] = (allOptionAmount[allOptionDates.indexOf(month)]* 100.0).roundToInt() / 100.00  // add the corresponding amount.
                }
            }

            optionSeries.data(optionData)

            Log.i(MYTAG, optionData.toList().toString())

            allSeries.add(optionSeries)

        }

        return if (allSeries.isEmpty()){
            AAChartModel()
                .chartType(AAChartType.Line)
                .title("Your Spendings Over Time")
                .yAxisTitle("Spendings (${viewModel.preferredCurrency.value})")
                .dataLabelsEnabled(true)
                .categories(allCategoriesDate.toTypedArray())
                .colorsTheme(arrayOf(
                    "#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2",
                    "#7f7f7f", "#bcbd22", "#17becf"
                ))
                .series(arrayOf(
                    AASeriesElement()
                        .name("Nothing To Display")
                        .data(arrayOf())))
        } else {
            AAChartModel()
                .chartType(AAChartType.Line)
                .title("Your Spendings Over Time")
                .yAxisTitle("Spendings (${viewModel.preferredCurrency.value})")
                .dataLabelsEnabled(true)
                .categories(allCategoriesDate.toTypedArray())
                .colorsTheme(arrayOf("#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2",
                    "#7f7f7f", "#bcbd22", "#17becf"))
                .series(allSeries.toTypedArray())
        }
    }

    private fun setupLineGraph(){
        chartViewLineGraph = binding.aaChartViewLine

        var chartViewModel : AAChartModel = buildAACharModelFrom(null)

        tsViewModel.lineGraph.value?.let {
            chartViewModel = buildAACharModelFrom(it)
            Log.i(MYTAG, "BUILD CONSTRUCTED")
        } ?: {
            Log.i(MYTAG, "NOT CONSTRUCTED")
        }
        chartViewLineGraph.aa_drawChartWithChartModel(chartViewModel)

        displayLineGraph()
    }

    private fun displayLineGraph(){
        tsViewModel.lineGraph.observe(viewLifecycleOwner){
            Log.i(MYTAG, "Building Chart Model From Answer : $it")
            chartViewLineGraph.aa_refreshChartWithChartModel(buildAACharModelFrom(it))
        }

//        filterViewModel.filteredTransactions.observe(viewLifecycleOwner){
//            Log.i(MYTAG, "Building Chart Model From Answer : $it")
//            chartViewLineGraph.aa_refreshChartWithChartModel(buildAACharModelFrom(filterViewModel.lineGraphAggregated.value))
//        }
    }

    private fun updateGraphTransactions() {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.getAllFilteredLineGraph(tsViewModel.filters)?.let {
                tsViewModel.updateLineGraph(it)
            } ?: tsViewModel.updateLineGraph(mutableListOf<OptionWithDateAndTotal>())
        }
    }

    private fun popupStartMonthDialog() {
        if (!binding.cbMonth.isChecked) {
            MonthYearPickerDialog(tsViewModel.date).apply {
                setListener { _, year, month, dayOfMonth ->
                    Toast.makeText(
                        requireContext(),
                        "Set date: $year/$month/$dayOfMonth",
                        Toast.LENGTH_LONG
                    ).show()
                    val monthYearFormatted = "$year-${String.format("%02d", month + 1)}"
                    tsViewModel.updateStartMonthAndYear(monthYearFormatted)
                }
                show(this@TimeSeriesFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }
    }

    private fun popupEndMonthDialog() {
        if (!binding.cbMonth.isChecked) {
            MonthYearPickerDialog(tsViewModel.date).apply {
                setListener { _, year, month, dayOfMonth ->
                    Toast.makeText(
                        requireContext(),
                        "Set date: $year/$month/$dayOfMonth",
                        Toast.LENGTH_LONG
                    ).show()
                    val monthYearFormatted = "$year-${String.format("%02d", month + 1)}"
                    tsViewModel.updateEndMonthAndYear(monthYearFormatted)
                }
                show(this@TimeSeriesFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }
    }

    private fun setupAutoCompleteTextView(){
        autoCompleteTextView = binding.actvCategory
        adapterCategory = ArrayAdapter(requireContext(),
            R.layout.spinner_currency_item,
            mutableListOf<String>())
        viewModel.categories.value?.let { adapterCategory.addAll(it) }
        autoCompleteTextView.setAdapter(adapterCategory)
        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, _, id ->
                val item = parent?.getItemAtPosition(id.toInt()).toString()
                tsViewModel.updateCat(item)
                binding.cbCat.isChecked = false
            }

        autoCompleteTextView.setText(tsViewModel.filters.cat.value)

        displayCategoriesList()
    }

    private fun displayCategoriesList(){
        viewModel.categories.observe(viewLifecycleOwner) {
            adapterCategory.clear()
            adapterCategory.addAll(it)
            adapterCategory.notifyDataSetChanged()
        }
    }

    private fun resetMonths() {
        binding.tvStartMonth.text = getString(R.string.start_month)
        binding.tvEndMonth.text = getString(R.string.end_month)
        binding.cbMonth.isChecked = true
    }

    private fun resetCat() {
        binding.actvCategory.text = null
        binding.cbCat.isChecked = true
        disableEditText(binding.actvCategory)
    }

    private fun disableEditText(editText: EditText){
        editText.focusable = View.NOT_FOCUSABLE
        editText.isFocusableInTouchMode = false
    }

    private fun enableEditText(editText: EditText){
        editText.focusable = View.FOCUSABLE
        editText.isFocusableInTouchMode = true
    }

    private fun Boolean.toInt() = if (this) 1 else 0

}