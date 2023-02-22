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
import kotlin.math.roundToInt

class TimeSeriesFragment : Fragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private val filterViewModel : FilterViewModel by activityViewModels()

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

        setupAutoCompleteTextView()

        filterViewModel.filters.startMonth.observe(viewLifecycleOwner) {
            if (it == "-1"){
                 if (filterViewModel.filters.endMonth.value == "-1") {
                     resetMonths()
                 }
            } else {
                binding.tvStartMonth.text = it
                binding.cbMonth.isChecked = false
            }
            updateGraphTransactions()
        }

        filterViewModel.filters.endMonth.observe(viewLifecycleOwner) {
            if (it == "-1"){
                if (filterViewModel.filters.startMonth.value == "-1") {
                    resetMonths()
                }
            } else {
                binding.tvEndMonth.text = it
                binding.cbMonth.isChecked = false
            }
            updateGraphTransactions()
        }

        filterViewModel.filters.cat.observe(viewLifecycleOwner) {
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
                    filterViewModel.updateCat("-1")
                }
                false -> {enableEditText(binding.actvCategory)}
            }
        }

        binding.cbMonth.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    filterViewModel.updateStartMonthAndYear("-1")
                    filterViewModel.updateEndMonthAndYear("-1")
                }
                false -> {}
            }
        }

//        viewModel.transactions.observe(viewLifecycleOwner){
//            updateGraphTransactions()
//        }

        updateGraphTransactions()
        loadCurrentData()
        setupLineGraph()
        updateGraphTransactions()

        return binding.root
    }

    private fun loadCurrentData() {
        filterViewModel.filters.startMonth.value = filterViewModel.filters.startMonth.value
        filterViewModel.filters.endMonth.value = filterViewModel.filters.endMonth.value
        filterViewModel.filters.cat.value = filterViewModel.filters.cat.value
        filterViewModel.lineGraphAggregated.value = filterViewModel.lineGraphAggregated.value
    }

    private fun buildAACharModelFrom(list: List<OptionWithDateAndTotal>?): AAChartModel {
        Log.i(MYTAG, "Before generating the chart model, the lineGraphAggregated is ${filterViewModel.lineGraphAggregated.value}")
        val defaultValue = AAChartModel()
            .chartType(AAChartType.Line)
            .title("Your Spendings Over Time")
            .dataLabelsEnabled(true)
            .categories(arrayOf("Nothing"))
            .series(arrayOf(
                AASeriesElement()
                    .name("Nothing To Display")
                    .data(arrayOf())))
        if (list ==  null) {
            Log.i(MYTAG, "BUILD AACHART RETURN BEC NULL")
            return defaultValue
        }

        var allSeries : MutableList<Any> = mutableListOf<Any>() // contains a list of AASeriesElement

        val allOptions = list.map { it -> it.option }.distinct()
        val allDates = list.map { it -> it.month }.distinct()
        val startMonth = allDates.min().split("-")
        val endMonth = allDates.max().split("-")

        val startM:Int = startMonth[1].toInt()
        val startY:Int = startMonth[0].toInt()

        val endM:Int = endMonth[1].toInt()
        val endY:Int = endMonth[0].toInt()

        val allCategoriesDateSize = endM-startM+1 + (endY-startY)*12

//        println("Number of months:$allCategoriesDateSize")

        var allCategoriesDate = List<String>(allCategoriesDateSize, init = {
                index -> "${startY + index / 12}-${String.format("%02d", 1+(startM - 1 + index % 12)%12)}"
        }) // (e.g. [2022-7, 2022-8])

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
                .dataLabelsEnabled(true)
                .categories(allCategoriesDate.toTypedArray())
                .series(arrayOf(
                    AASeriesElement()
                        .name("Nothing To Display")
                        .data(arrayOf())))
        } else {
            AAChartModel()
                .chartType(AAChartType.Line)
                .title("Your Spendings Over Time")
                .dataLabelsEnabled(true)
                .categories(allCategoriesDate.toTypedArray())
                .series(allSeries.toTypedArray())
        }
    }

    private fun setupLineGraph(){
        chartViewLineGraph = binding.aaChartViewLine

        var chartViewModel : AAChartModel = buildAACharModelFrom(null)

        filterViewModel.lineGraphAggregated.value?.let {
            chartViewModel = buildAACharModelFrom(it)
            Log.i(MYTAG, "BUILD CONSTRUCTED")
        } ?: {
            Log.i(MYTAG, "NOT CONSTRUCTED")
        }
        chartViewLineGraph.aa_drawChartWithChartModel(chartViewModel)

        displayLineGraph()
    }

    private fun displayLineGraph(){
        filterViewModel.lineGraphAggregated.observe(viewLifecycleOwner){
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
            viewModel.getAllFilteredLineGraph(filterViewModel.filters)?.let {
                filterViewModel.updateLineGraph(it)
            } ?: filterViewModel.updateLineGraph(mutableListOf<OptionWithDateAndTotal>())
        }
    }

    private fun popupStartMonthDialog() {
        if (!binding.cbMonth.isChecked) {
            MonthYearPickerDialog(filterViewModel.date).apply {
                setListener { _, year, month, dayOfMonth ->
                    Toast.makeText(
                        requireContext(),
                        "Set date: $year/$month/$dayOfMonth",
                        Toast.LENGTH_LONG
                    ).show()
                    val monthYearFormatted = "${String.format("%02d", month + 1)}-$year"
                    filterViewModel.updateStartMonthAndYear(monthYearFormatted)
                }
                show(this@TimeSeriesFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }
    }

    private fun popupEndMonthDialog() {
        if (!binding.cbMonth.isChecked) {
            MonthYearPickerDialog(filterViewModel.date).apply {
                setListener { _, year, month, dayOfMonth ->
                    Toast.makeText(
                        requireContext(),
                        "Set date: $year/$month/$dayOfMonth",
                        Toast.LENGTH_LONG
                    ).show()
                    val monthYearFormatted = "${String.format("%02d", month + 1)}-$year"
                    filterViewModel.updateEndMonthAndYear(monthYearFormatted)
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
                filterViewModel.updateCat(item)
                binding.cbCat.isChecked = false
            }

        autoCompleteTextView.setText(filterViewModel.filters.cat.value)

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