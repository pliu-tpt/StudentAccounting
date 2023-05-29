package com.example.studentaccounting

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccounting.databinding.TransactionDayItemBinding
import com.example.studentaccounting.databinding.TransactionItemBinding
import com.example.studentaccounting.db.entities.relations.TransactionGroup
import com.example.studentaccounting.db.entities.relations.TransactionWithConversion
import java.text.SimpleDateFormat
import kotlin.math.absoluteValue

class TransactionGroupRecyclerViewAdapter(
    private var preferredCurrency: String,
    private val clickListener: (TransactionWithConversion, View) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val transactionList = ArrayList<TransactionWithConversion>() // list of transactions
    private val transactionGroups = ArrayList<TransactionGroup>() // groupTransactionsByDate(transactionList)

    private fun groupTransactionsByDate(transactions: List<TransactionWithConversion>): List<TransactionGroup> {
        val groups = mutableListOf<TransactionGroup>()

        transactions.groupBy { it.transaction.date }.forEach { (date, transactions) ->
            val totalAmount = transactions.sumOf { if (it.transaction.isSpending) it.preferred_currency_amount else -1.0 * it.preferred_currency_amount}
            groups.add(TransactionGroup(date, totalAmount, transactions))
        }

        return groups
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            GROUP_ITEM_VIEW_TYPE -> {
                val binding = TransactionDayItemBinding.inflate(inflater,parent, false)
                TransactionDayViewHolder(binding)
            }
            TRANSACTION_ITEM_VIEW_TYPE -> {
                val binding = TransactionItemBinding.inflate(inflater,parent, false)
                return TransactionGroupViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder.itemViewType) {
            GROUP_ITEM_VIEW_TYPE -> { // per day total
                val groupViewHolder = holder as TransactionDayViewHolder
                val group = item as TransactionGroup
                groupViewHolder.bind(group, preferredCurrency)
            }
            TRANSACTION_ITEM_VIEW_TYPE -> { // transactions
                val transactionGroupViewHolder = holder as TransactionGroupViewHolder
                val transaction = item as TransactionWithConversion
                transactionGroupViewHolder.bind(transaction, clickListener)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return transactionGroups.sumOf { it.transactions.size + 1 } // Include group items in count
    }

    private fun getItem(position: Int): Any {
        var count = 0
        for (group in transactionGroups) {
            if (position == count) {
                return group
            }
            count += group.transactions.size + 1 // Add 1 for group item
            if (position < count) {
                return group.transactions[position - count + group.transactions.size]
            }
        }
        throw IllegalArgumentException("Invalid position")
    }

    companion object {
        private const val GROUP_ITEM_VIEW_TYPE = 0
        private const val TRANSACTION_ITEM_VIEW_TYPE = 1
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item) {
            is TransactionGroup -> GROUP_ITEM_VIEW_TYPE
            is TransactionWithConversion -> TRANSACTION_ITEM_VIEW_TYPE
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    fun updateTransactionGroups(){
        transactionGroups.clear()
        transactionGroups.addAll(groupTransactionsByDate(transactionList))
    }

    fun setList(transactions:List<TransactionWithConversion>){
        transactionList.clear()
        transactionList.addAll(transactions)
        updateTransactionGroups()
    }

    fun setPreferredCurrency(prefCurrency: String){
        preferredCurrency = prefCurrency
    }

}

class TransactionGroupViewHolder(private val binding: TransactionItemBinding): RecyclerView.ViewHolder(binding.root){

    @SuppressLint("SimpleDateFormat")
    private val originalFormatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    @SuppressLint("SimpleDateFormat")
    private val destinationFormatter : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
    fun bind(t: TransactionWithConversion, clickListener: (TransactionWithConversion, View) -> Unit){
        binding.apply {
            val transaction = t.transaction

            tvAmountCard.text = String.format("%.2f",t.preferred_currency_amount)
            tvCurrencyCard.text = t.preferred_currency

            val plusMinus = if (transaction.isSpending) "- " else "+ "
            tvAmountCardReal.text = "$plusMinus${transaction.amount}"

            tvCatCard.text = transaction.category
            tvSubcatCard.text = transaction.subcategory
            tvNameCard.text = transaction.name
            tvCurrencyCardReal.text = transaction.currency
            tvType.text = transaction.type

            tvDate.text = destinationFormatter.format(originalFormatter.parse(transaction.date))

            tvAmountCard.setTextColor(
                ContextCompat.getColor(
                    super.itemView.context,
                    if (transaction.isSpending) R.color.spending else R.color.earning
                )
            )

            root.setOnLongClickListener {
                clickListener(t, binding.root)
                return@setOnLongClickListener true
            }
        }
    }

}

//Per day total
class TransactionDayViewHolder(private val binding: TransactionDayItemBinding): RecyclerView.ViewHolder(binding.root){

    @SuppressLint("SimpleDateFormat")
    private val originalFormatter : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    @SuppressLint("SimpleDateFormat")
    private val destinationFormatter : SimpleDateFormat = SimpleDateFormat("EEE dd MMMM yyyy")

    fun bind(transactionGroup: TransactionGroup, preferredCurrency: String){
        binding.apply {

            tvDayDate.text = destinationFormatter.format(originalFormatter.parse(transactionGroup.option))
            tvDayTotal.text = if (transactionGroup.total > 0) String.format("-%.2f", transactionGroup.total) else String.format("+%.2f", transactionGroup.total.absoluteValue)
            tvDayCurrency.text = preferredCurrency

//            val color = ContextCompat.getColor(
//                super.itemView.context,
//                if (transactionGroup.total > 0) R.color.spending else R.color.earning
//            )
//
//            tvDayTotal.setTextColor(color)
//            tvDayCurrency.setTextColor(color)

            root.setOnLongClickListener {

                return@setOnLongClickListener true
            }
        }
    }

}


