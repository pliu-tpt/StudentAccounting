package com.example.studentaccounting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentaccounting.databinding.FragmentTransactionListBinding
import com.example.studentaccounting.db.Filters
import com.example.studentaccounting.db.entities.Transaction
import com.example.studentaccounting.db.entities.relations.TransactionWithConversion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.reflect.Type


class TransactionListFragment : Fragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private lateinit var binding: FragmentTransactionListBinding

    private lateinit var adapter: TransactionGroupRecyclerViewAdapter

    private lateinit var aggAdapter: AggregateTypeRecyclerViewAdapter

    private lateinit var extension : String

    companion object {
        const val MYTAG = "MYTAG"
    }

    private var resultExportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result -> if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            if (uri != null) {
                try {
                    val outputStream = activity?.contentResolver?.openOutputStream(uri)
                    if (outputStream != null) {
                        when (extension) {
                            "csv" -> exportTransactionsToCSVFile(outputStream)
                            "json" -> exportTransactionsToJSONFile(outputStream)
                            else -> Log.i(MYTAG, "Something went wrong at $uri")
                        }
                        Toast.makeText(context, "Database exported to $uri", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error exporting database: $e", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.i(MYTAG, "URI/Intent INVALID")
            }

        }
    }

    private var resultImportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result -> if (result.resultCode == Activity.RESULT_OK) {
        val data: Intent? = result.data
        val uri = data?.data
        if (uri != null) {
            try {
                val inputStream = activity?.contentResolver?.openInputStream(uri)
                if (inputStream != null) {
                    when (extension) {
                        "csv" -> importTransactionsFromCSVFile(inputStream)
                        "json" -> importTransactionsFromJSONFile(inputStream)
                        else -> Log.i(MYTAG, "Something went wrong at $uri")
                    }
                    Toast.makeText(context, "Database imported from $uri", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error importing database: $e", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.i(MYTAG, "URI/Intent INVALID")
        }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTransactionListBinding.inflate(inflater, container, false)

        initRecyclerView()
        initRVType()

        binding.btnAddNew.setOnClickListener {
            it.findNavController().navigate(R.id.action_transactionListFragment_to_selectIsSpendingFragment)
        }

        binding.btnDownload.setOnClickListener {
            openExportMenu(it)
        }

        binding.btnUpload.setOnClickListener {
            openImportMenu(it)
        }

//        val navHostFragment = childFragmentManager.findFragmentById(R.id.topLayout) as NavHostFragment
//        val navController = navHostFragment.navController
        // hide nav bar when entering a transaction
//        navController.addOnDestinationChangedListener{
//                _, destination, _ ->
//            run {
//                val listOfFragments = arrayOf(
//                    R.id.selectIsSpendingFragment,
//                    R.id.selectAmountFragment,
//                    R.id.selectCategoryFragment,
//                    R.id.selectSubcategoryFragment,
//                    R.id.selectTransactionTypeFragment,
//                    R.id.selectTransactionTypeFromFragment,
//                    R.id.selectTransactionTypeToFragment,
//                    R.id.transactionSummaryFragment)
//
//                Log.i(MYTAG, "The nav id is ${destination.id}, ${destination.displayName}")
//
//                if (destination.id in listOfFragments) {
//                    binding.bNavView.visibility = View.GONE
//                    this.supportActionBar?.hide()
//                } else {
//                    activityMainBinding.bNavView.visibility = View.VISIBLE
//                    this.supportActionBar?.show()
//                }
//            }
//        }

        return binding.root
    }

    private fun startCreateDocumentActivityForResult(ext:String){
        when (ext) {
            "csv" -> try {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/comma-separated-values" // "application/json"
                    putExtra(Intent.EXTRA_TITLE, "data.csv")
                }
                extension = ext
                resultExportLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Error exporting database: $e", Toast.LENGTH_SHORT).show()
            }
            "json" -> try {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "data.json")
                }
                extension = ext
                resultExportLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Error exporting database: $e", Toast.LENGTH_SHORT).show()
            }
            else -> Toast.makeText(context, "Only json and csv are supported.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSelectDocumentActivityForResult(ext:String){
        when (ext) {
            "csv" -> try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*"
                }
                extension = ext
                resultImportLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Error exporting database: $e", Toast.LENGTH_SHORT).show()
            }
            "json" -> try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                }
                extension = ext
                resultImportLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Error exporting database: $e", Toast.LENGTH_SHORT).show()
            }
            else -> Toast.makeText(context, "Only json and csv are supported.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun initRVType(){

        binding.rvTypeAggregate.layoutManager = LinearLayoutManager(requireContext())
        aggAdapter = AggregateTypeRecyclerViewAdapter(viewModel.preferredCurrency.value!!){}
        binding.rvTypeAggregate.adapter = aggAdapter

        displayRVType()
    }

    private fun displayRVType(){
        viewModel.preferredCurrency.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getTypeAggregate()?.let { it1 -> aggAdapter.setList(it1) }
                aggAdapter.setPreferredCurrency(viewModel.preferredCurrency.value!!)
                aggAdapter.notifyDataSetChanged()
            }
        }
        viewModel.transactions.observe(viewLifecycleOwner){
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getTypeAggregate()
                    ?.let { it1 ->
                        aggAdapter.setList(it1)
                        aggAdapter.notifyDataSetChanged()
                    }
            }
        }
    }

    private fun initRecyclerView(){

        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        adapter = TransactionGroupRecyclerViewAdapter(viewModel.preferredCurrency.value!!){
            selectedTransaction, view -> transactionListItemLongClicked(selectedTransaction, view)
        }
        binding.rvTransactions.adapter = adapter

        displayTransactionTypesList()
    }

    private fun displayTransactionTypesList(){
        viewModel.preferredCurrency.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getAllFilteredWithPrefCurrency(Filters(prefCurrency = viewModel.preferredCurrency, isSortedByDate = true))?.let { it1 -> adapter.setList(it1) }
                adapter.setPreferredCurrency(viewModel.preferredCurrency.value!!)
                adapter.notifyDataSetChanged()
            }
        }

//        viewModel.currencies.observe(viewLifecycleOwner) {
//            CoroutineScope(Dispatchers.Main).launch {
//                viewModel.getAllFilteredWithPrefCurrency(Filters(prefCurrency = viewModel.preferredCurrency))?.let { it1 -> adapter.setList(it1) }
//                adapter.notifyDataSetChanged()
//            }
//        }

        viewModel.transactions.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getAllFilteredWithPrefCurrency(Filters(prefCurrency = viewModel.preferredCurrency, isSortedByDate = true))?.let { it1 -> adapter.setList(it1) }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun openExportMenu(view: View){
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.popup_export_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.json -> {
                    startCreateDocumentActivityForResult("json")
                    true
                }
                R.id.csv -> {
                    startCreateDocumentActivityForResult("csv")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun openImportMenu(view: View){
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.popup_import_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.json -> {
                    startSelectDocumentActivityForResult("json")
                    true
                }
                R.id.csv -> {
                    startSelectDocumentActivityForResult("csv")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun transactionListItemLongClicked(t: TransactionWithConversion, view: View){
        val transaction = t.transaction
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.popup_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    // Handle Edit option
                    Log.i("MYTAG", "Edit transaction number ${transaction.id}")
                    viewModel.updateTransactionToEdit(transaction)
                    requireView().findNavController().navigate(R.id.action_transactionListFragment_to_editTransactionFragment)
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

    private fun exportTransactionsToCSVFile(outputStream: OutputStream) {
        val csvWriter = CSVWriter(outputStream.bufferedWriter())
        csvWriter.writeNext(arrayOf("transaction_id",
            "transaction_name",
            "transaction_category",
            "transaction_subcategory",
            "transaction_type",
            "transaction_amount",
            "transaction_currency",
            "isSpending",
            "transaction_date")
        )

        viewModel.transactions.value?.forEach {
            csvWriter.writeNext(arrayOf(it.id.toString(),
                it.name,
                it.category,
                it.subcategory,
                it.type,
                it.amount.toString(),
                it.currency,
                it.isSpending.toString(),
                it.date)
            )
        }
        csvWriter.close()
        outputStream.close()
    }

    private fun exportTransactionsToJSONFile(outputStream: OutputStream) {

        val listType : Type = object : TypeToken<List<Transaction>>(){}.type
        val transactionsJson : String = Gson().toJson(viewModel.transactions.value, listType)

        outputStream.write(transactionsJson.toByteArray())
        outputStream.close()
    }

    private fun importTransactionsFromCSVFile(inputStream: InputStream) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val csv = CSVReader(reader)

        val data = csv.readAll()
        data.stream().skip(1).forEach { row ->
            val id = row[0]
            val name = row[1]
            val cat = row[2]
            val subCat = row[3]
            val type = row[4]
            val amount = row[5]
            val currency = row[6]
            val isSpending = row[7]
            val date = row[8]
            Log.i(MYTAG, "$id,$name,$cat,$subCat,$type,$amount,$currency,$isSpending,$date")

            if (name.isNullOrBlank() || cat.isNullOrBlank() || subCat.isNullOrBlank()|| type.isNullOrBlank()|| amount.isNullOrBlank()|| currency.isNullOrBlank()|| isSpending.isNullOrBlank()|| date.isNullOrBlank()) {
                return@forEach
            } else {
                viewModel.insertTransaction(Transaction(0, name, cat, subCat, type, amount.toDouble(), currency, isSpending.toBoolean(), date))
            }
        }
        inputStream.close()
    }

    private fun importTransactionsFromJSONFile(inputStream: InputStream) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val jsonString = reader.readText()

        // the single line of JSON contains a list of Transaction
        val listType : Type = object : TypeToken<List<Transaction>>(){}.type
        val data : List<Transaction> = Gson().fromJson(jsonString, listType)
        for (item in data) {
            viewModel.insertTransactionAnyID(item)
        }
        inputStream.close()
    }
}
