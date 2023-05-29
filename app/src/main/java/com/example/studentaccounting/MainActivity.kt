package com.example.studentaccounting

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.studentaccounting.databinding.ActivityMainBinding
import com.example.studentaccounting.db.AppDatabase
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.launch

//import com.example.studentaccounting.db.TransactionDatabase

//import nl.hiddewieringa.money.monetaryContext
//import nl.hiddewieringa.money.ofCurrency
//import org.javamoney.moneta.FastMoney
//import java.math.MathContext

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var viewModel: TransactionViewModel

    private lateinit var filterViewModel: FilterViewModel
    private lateinit var budgetViewModel: BudgetViewModel
    private lateinit var timeSeriesViewModel: TimeSeriesViewModel
    private lateinit var countsViewModel: CountsViewModel

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerViewAdapter: PagerViewAdapter

    private val mOnItemSelectedListener = NavigationBarView.OnItemSelectedListener setOnItemSelectedListener@{
            item ->
        when (item.itemId) {
            R.id.transactionListContainerFragment -> {
                viewPager.currentItem = 0
                return@setOnItemSelectedListener true
            }
            R.id.filterFragment -> {
                viewPager.currentItem = 1
                return@setOnItemSelectedListener true
            }
            R.id.timeSeriesFragment -> {
                viewPager.currentItem = 2
                return@setOnItemSelectedListener true
            }
            R.id.budgetFragment -> {
                viewPager.currentItem = 3
                return@setOnItemSelectedListener true
            }
            R.id.countsFragment -> {
                viewPager.currentItem = 4
                return@setOnItemSelectedListener true
            }
        }
        false
    }

//    val fragmentHome: Fragment = TransactionListFragment()
//    val fragmentFilter: Fragment = FilterFragment()
//    val fragmentTimeSeries: Fragment = TimeSeriesFragment()
//    val fragmentBudget: Fragment = BudgetFragment()
//    val fragmentCounts: Fragment = CountsFragment()

//    val fm: FragmentManager = supportFragmentManager
//    var activeFragment = fragmentHome

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val dao = AppDatabase.getInstance(application)!!.transactionDao()
        val currencyDao = AppDatabase.getInstance(application)!!.currencyDao()

        Log.i("WOW", dao.getAllCurrency().value.toString())

        val factory = TransactionViewModelFactory(dao,currencyDao)
        viewModel = ViewModelProvider(this,factory)[TransactionViewModel::class.java]

        filterViewModel = ViewModelProvider(this)[FilterViewModel::class.java]
        budgetViewModel = ViewModelProvider(this)[BudgetViewModel::class.java]
        timeSeriesViewModel = ViewModelProvider(this)[TimeSeriesViewModel::class.java]

        countsViewModel = ViewModelProvider(this)[CountsViewModel::class.java]

        // Pager View related
        viewPager = activityMainBinding.mainViewPager

        pagerViewAdapter = PagerViewAdapter(this)
        viewPager.adapter = pagerViewAdapter

//        viewPager.offscreenPageLimit = 5

        // Pager notifying the BNView of a change
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                activityMainBinding.bNavView.menu.getItem(position).isChecked = true
            }
        })

        // BNView notifying the Pager of a change
        activityMainBinding.bNavView.setOnItemSelectedListener(mOnItemSelectedListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                showChangeDefaultDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showChangeDefaultDialog() {
        // val options = viewModel.currencies.value

        var dialog: AlertDialog? = null

        val adapter = OptionAdapter(emptyList()) { option ->
            // update default value in view model
            viewModel.preferredCurrency.value = option
            dialog?.dismiss()
        }

        lifecycleScope.launch {
            val options = viewModel.getAllCurrencies() // Warning: this call is synchronous because light
            Log.i("WOW",options.toString())
            adapter.updateOptions(options)
        }

        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        dialog = AlertDialog.Builder(this)
            .setTitle("Change Preferred Currency")
            .setNegativeButton("Cancel", null)
            .setView(recyclerView)
            .create()

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.preferred_currency_menu, menu)

        viewModel.currencies.observe(this){

        }

        return true
    }
}

// Adapter related to the Pager
class PagerViewAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 5
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TransactionListContainerFragment()
            1 -> FilterContainerFragment()
            2 -> TimeSeriesFragment()
            3 -> BudgetFragment()
            4 -> CountsFragment()
            else -> TransactionListContainerFragment()
        }
    }
}

class OptionAdapter(
    private var options: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<OptionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.textView.text = option
        holder.itemView.setOnClickListener { onClick(option) }
    }

    override fun getItemCount(): Int = options.size

    fun updateOptions(newOptions: List<String>){
        options = newOptions
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }
}

// TODO("Import/Export Table")
// TODO("Retrofit, App Data for Google Drive: Synchronize databases")
// TODO("General Settings : Add a possibility to change prefCurrency")
// TODO("Line Graphs : Select two months and a cat and the line graph is generated using AACharts")
// TODO("A notification button to add a transaction really quickly")

// Test Graph Commit