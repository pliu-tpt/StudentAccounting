package com.example.studentaccounting

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccounting.databinding.TransactionItemBinding
import com.example.studentaccounting.db.entities.relations.TransactionWithConversion
import java.text.SimpleDateFormat

class TransactionRecyclerViewAdapter(
    private val clickListener: (TransactionWithConversion, View) -> Unit
) : RecyclerView.Adapter<TransactionViewHolder>() {

    private val transactionList = ArrayList<TransactionWithConversion>() // list of transactions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = TransactionItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return TransactionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactionList[position], clickListener)
    }

    fun setList(transactions:List<TransactionWithConversion>){
        transactionList.clear()
        transactionList.addAll(transactions)
    }

}

class TransactionViewHolder(private val binding: TransactionItemBinding): RecyclerView.ViewHolder(binding.root){

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