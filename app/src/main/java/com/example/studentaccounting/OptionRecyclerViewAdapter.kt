package com.example.studentaccounting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studentaccounting.databinding.SelectItemBinding
import com.example.studentaccounting.db.entities.relations.OptionWithTotal

class OptionRecyclerViewAdapter(
    private val clickListener:(String)->Unit
): RecyclerView.Adapter<OptionViewHolder>() {

    private val optionList = ArrayList<String>() // list of options

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = SelectItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return OptionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return optionList.size
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(optionList[position], clickListener)
    }

    fun setList(options:List<String>){
        optionList.clear()
        optionList.addAll(options)
    }

}

class OptionViewHolder(private val binding: SelectItemBinding): RecyclerView.ViewHolder(binding.root){
    fun bind(option: String, clickListener: (String) -> Unit){
        binding.apply {
            btnOption.text = option
            btnOption.setOnClickListener{
                // implement something that says to go to the next page
                clickListener(option)
            }
        }
    }
}