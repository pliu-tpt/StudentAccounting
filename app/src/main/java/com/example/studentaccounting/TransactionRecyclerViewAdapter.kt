package com.example.studentaccounting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccounting.databinding.TransactionItemBinding
import com.example.studentaccounting.db.entities.Transaction
import com.example.studentaccounting.db.entities.relations.TransactionWithConversion

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
    fun bind(t: TransactionWithConversion, clickListener: (TransactionWithConversion, View) -> Unit){
        binding.apply {
            val transaction = t.transaction

            tvAmountCard.text = String.format("%.2f",t.preferred_currency_amount)
            tvCurrencyCard.text = t.preferred_currency

            var plusMinus = if (transaction.isSpending) "- " else "+ "
            tvAmountCardReal.text = plusMinus + transaction.amount.toString()

            tvCatCard.text = transaction.category
            tvSubcatCard.text = transaction.subcategory
            tvNameCard.text = transaction.name
            tvCurrencyCardReal.text = transaction.currency

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