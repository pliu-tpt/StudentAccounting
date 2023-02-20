package com.example.studentaccounting

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.studentaccounting.TransactionListFragment.Companion.MYTAG
import com.example.studentaccounting.databinding.ActivityMainBinding
import com.example.studentaccounting.db.AppDatabase
//import com.example.studentaccounting.db.TransactionDatabase

//import nl.hiddewieringa.money.monetaryContext
//import nl.hiddewieringa.money.ofCurrency
//import org.javamoney.moneta.FastMoney
//import java.math.MathContext

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var viewModel: TransactionViewModel

    private lateinit var filterViewModel: FilterViewModel
    private lateinit var countsViewModel: CountsViewModel
    private lateinit var timeSeriesViewModel: TimeSeriesViewModel

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

        val factory = TransactionViewModelFactory(dao,currencyDao)
        viewModel = ViewModelProvider(this,factory)[TransactionViewModel::class.java]

        filterViewModel = ViewModelProvider(this)[FilterViewModel::class.java]
        timeSeriesViewModel = ViewModelProvider(this)[TimeSeriesViewModel::class.java]

        countsViewModel = ViewModelProvider(this)[CountsViewModel::class.java]

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.topLayout) as NavHostFragment
        activityMainBinding.bNavView.setupWithNavController(navHostFragment.navController)

        // hide nav bar when entering a transaction
        navHostFragment.navController.addOnDestinationChangedListener{
            _, destination, _ ->
            run {
                val listOfFragments = arrayOf(
                    R.id.selectIsSpendingFragment,
                    R.id.selectAmountFragment,
                    R.id.selectCategoryFragment,
                    R.id.selectSubcategoryFragment,
                    R.id.selectTransactionTypeFragment,
                    R.id.selectTransactionTypeFromFragment,
                    R.id.selectTransactionTypeToFragment,
                    R.id.transactionSummaryFragment)

                Log.i(MYTAG, "The nav id is ${destination.id}, ${destination.displayName}")

                if (destination.id in listOfFragments) {
                    activityMainBinding.bNavView.visibility = View.GONE
                    this.supportActionBar?.hide()
                } else {
                    activityMainBinding.bNavView.visibility = View.VISIBLE
                    this.supportActionBar?.show()
                }
            }
        }

        // TODO("Import/Export Table")
        // TODO("Retrofit, App Data for Google Drive: Synchronize databases")
        // TODO("General Settings : Add a possibility to change prefCurrency")
        // TODO("Line Graphs : Select two months and a cat and the line graph is generated using AACharts")
        // TODO("A notification button to add a transaction really quickly")

        // Test Graph Commit

    }

}

