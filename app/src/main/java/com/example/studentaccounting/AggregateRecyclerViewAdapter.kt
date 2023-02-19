package com.example.studentaccounting

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccounting.TransactionListFragment.Companion.MYTAG
import com.example.studentaccounting.databinding.OptionTotalItemBinding
import com.example.studentaccounting.databinding.SelectItemBinding
import com.example.studentaccounting.db.entities.relations.OptionWithTotal
import java.security.cert.PKIXRevocationChecker.Option
import kotlin.math.abs

class AggregateRecyclerViewAdapter(
    private val aggCurrency: String,
    private val clickListener:(OptionWithTotal)->Unit
): RecyclerView.Adapter<OptionAggregateViewHolder>() {

    private val optionList = ArrayList<OptionWithTotal>() // list of options

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionAggregateViewHolder {
        val binding = OptionTotalItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return OptionAggregateViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return optionList.size
    }

    override fun onBindViewHolder(holder: OptionAggregateViewHolder, position: Int) {
        holder.bind(optionList[position], clickListener, position, aggCurrency)
    }

    fun setList(options:List<OptionWithTotal>){
        optionList.clear()
        optionList.addAll(options)
    }

     fun addTotal(){
        Log.i(MYTAG,"INSERT TOTAL ?")
        var total = 0.0
        optionList.forEach {
            total += it.total
        }
        optionList.add(OptionWithTotal("TOTAL SPENDING", total))
    }

}

class OptionAggregateViewHolder(private val binding: OptionTotalItemBinding): RecyclerView.ViewHolder(binding.root){
    @SuppressLint("SetTextI18n")
    fun bind(option:OptionWithTotal, clickListener:(OptionWithTotal)->Unit, position: Int, aggCurrency: String){
        binding.apply {

            tvOption.textSize = 14F
            tvTotal.textSize = 14F
            tvOption.setTypeface(null, Typeface.NORMAL)
            tvTotal.setTypeface(null, Typeface.NORMAL)

            if (option.total >= 0) {
                tvOption.text = "   +   ${option.option}"
                with (R.color.spending) {
                    tvOption.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                    tvTotal.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                }
            } else {
                tvOption.text = "   -   ${option.option}"
                with (R.color.earning) {
                    tvOption.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                    tvTotal.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                }
            }

            if (option.option == "TOTAL SPENDING"){
                tvOption.text = "       ${option.option}"
                tvOption.textSize = 18F
                tvTotal.textSize = 18F
                tvOption.setTypeface(null, Typeface.BOLD)
                tvTotal.setTypeface(null, Typeface.BOLD)
                with (R.color.black) {
                    tvOption.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                    tvTotal.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                }
            }

            if (position == 0) {
                tvOption.text = "       ${option.option}"
            }

            tvTotal.text = "${String.format("%.2f",abs(option.total))} $aggCurrency"

            this.root.setOnClickListener{
                // eg change the option to the selected category
                clickListener(option)
            }
        }
    }
}