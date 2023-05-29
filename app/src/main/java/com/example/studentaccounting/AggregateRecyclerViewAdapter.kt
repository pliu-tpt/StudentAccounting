package com.example.studentaccounting

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccounting.TransactionListFragment.Companion.MYTAG
import com.example.studentaccounting.databinding.OptionTotalItemBinding
import com.example.studentaccounting.db.entities.relations.OptionWithTotal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class AggregateRecyclerViewAdapter(
    private var aggCurrency: String,
    private val clickListener:(OptionWithTotal)->Unit
): RecyclerView.Adapter<OptionAggregateViewHolder>() {

    private val optionList = ArrayList<OptionWithTotal>() // list of options
    private var isAllTime : Boolean = false
    private var averageMode = "/month"
    private var startingMonth = "" // the starting month from which we count months or days.
    private var divideBy : Long = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionAggregateViewHolder {
        val binding = OptionTotalItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return OptionAggregateViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return optionList.size
    }

    override fun onBindViewHolder(holder: OptionAggregateViewHolder, position: Int) {
        holder.bind(optionList[position], clickListener, position, aggCurrency, averageMode, divideBy)
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
        optionList.add(OptionWithTotal("TOTAL SPENT", total))
    }

    fun updatePrefCurrency(prefCurrency:String){
        aggCurrency = prefCurrency
    }

    fun updateIsAllTime(boolean: Boolean, startMonth: String){
        isAllTime = boolean
        startingMonth = startMonth

        val parsedDate = LocalDate.parse(startingMonth)
        val now = LocalDate.now()

        if (isAllTime){
            averageMode = "/month"
            divideBy = ChronoUnit.MONTHS.between(parsedDate, now)
        } else {
            averageMode = "/day"
            divideBy = if (parsedDate.month == now.month && parsedDate.year == now.year) {
                ChronoUnit.DAYS.between(parsedDate, LocalDate.now())
            } else {
                parsedDate.lengthOfMonth().toLong()
            }
        }
    }
}

class OptionAggregateViewHolder(private val binding: OptionTotalItemBinding): RecyclerView.ViewHolder(binding.root){
    @SuppressLint("SetTextI18n")
    fun bind(option:OptionWithTotal, clickListener:(OptionWithTotal)->Unit, position: Int, aggCurrency: String, averageMode:String, divideBy:Long){
        binding.apply {

            tvOption.textSize = 14F
            tvTotal.textSize = 14F
            tvOption.setTypeface(null, Typeface.NORMAL)
            tvTotal.setTypeface(null, Typeface.NORMAL)

            tvAverage.text = "${String.format("%.0f",abs(option.total)/divideBy)}$aggCurrency$averageMode"
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

            if (position == 0) {
                tvOption.text = "       ${option.option}"
            }

            tvTotal.text = "${String.format("%.2f",abs(option.total))} $aggCurrency"

            if (option.option == "TOTAL SPENT"){
                tvOption.text = "       ${option.option}"
                tvTotal.text = "${String.format("%.2f",option.total)} $aggCurrency"

                tvOption.textSize = 16F
                tvTotal.textSize = 16F
                tvOption.setTypeface(null, Typeface.BOLD)
                tvTotal.setTypeface(null, Typeface.BOLD)
                with (R.color.black) {
                    tvOption.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                    tvTotal.setTextColor(ContextCompat.getColor(super.itemView.context, this))
                }
            }



            this.root.setOnClickListener{
                // eg change the option to the selected category
                clickListener(option)
            }
        }
    }
}