package com.example.studentaccounting

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccounting.databinding.OptionTypeTotalItemBinding
import com.example.studentaccounting.db.entities.relations.OptionWithTotal
import kotlin.math.abs

class AggregateTypeRecyclerViewAdapter(
    private var aggCurrency: String,
    private val clickListener:(OptionWithTotal)->Unit
): RecyclerView.Adapter<OptionAggregateTypeViewHolder>() {

    private val optionList = ArrayList<OptionWithTotal>() // list of options

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionAggregateTypeViewHolder {
        val binding = OptionTypeTotalItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return OptionAggregateTypeViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return optionList.size
    }

    override fun onBindViewHolder(holder: OptionAggregateTypeViewHolder, position: Int) {
        holder.bind(optionList[position], clickListener, position, aggCurrency)
    }

    fun setList(options:List<OptionWithTotal>){
        optionList.clear()
        optionList.addAll(options)
    }

    fun setPreferredCurrency(prefCurrency: String){
        aggCurrency = prefCurrency
    }

}

class OptionAggregateTypeViewHolder(private val binding: OptionTypeTotalItemBinding): RecyclerView.ViewHolder(binding.root){
    @SuppressLint("SetTextI18n")
    fun bind(option:OptionWithTotal, clickListener:(OptionWithTotal)->Unit, position: Int, aggCurrency: String){
        binding.apply {
            tvOption.setTypeface(null, Typeface.NORMAL)
            tvTotal.setTypeface(null, Typeface.NORMAL)
            tvOption.text = "${option.option}"
            if (option.total >= 0) {
                with (R.color.spending) {
                    tvOption.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                    tvTotal.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                }
            } else {
                with (R.color.earning) {
                    tvOption.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                    tvTotal.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                }
            }
            tvTotal.text = "${String.format("%.2f",abs(option.total))} $aggCurrency"
        }
    }
}