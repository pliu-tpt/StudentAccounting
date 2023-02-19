package com.example.studentaccounting

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.studentaccounting.databinding.FragmentSelectTransactionTypeBinding
import com.example.studentaccounting.databinding.FragmentSelectTransactionTypeFromBinding
import com.example.studentaccounting.databinding.FragmentSelectTransactionTypeToBinding

class SelectTransactionTypeToFragment : SelectFragment() {
    private val viewModel : TransactionViewModel by activityViewModels()
    private lateinit var binding: FragmentSelectTransactionTypeToBinding

    private lateinit var adapter: OptionRecyclerViewAdapter

    init {
        nextPageResId = R.id.action_selectTransactionTypeToFragment_to_selectAmountFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSelectTransactionTypeToBinding.inflate(inflater, container, false)


        initRecyclerView()

        viewModel.selectedType.observe(viewLifecycleOwner) {
            binding.etNewTypeTo.setText(it.toString())
        }

        binding.btnTypeNextTo.setOnClickListener {
            if (!TextUtils.isEmpty(binding.etNewTypeTo.text.toString())){
                nextPageAction(binding.etNewTypeTo.text.toString())
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please Enter a Valid Type",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return binding.root
    }

    private fun initRecyclerView(){

        binding.rvTransactionTypeTo.layoutManager = GridLayoutManager(context, 2)
        adapter = OptionRecyclerViewAdapter{
                selectedOption: String -> nextPageAction(selectedOption)
        }
        binding.rvTransactionTypeTo.adapter = adapter


        displayTransactionTypesList()
    }
    private fun displayTransactionTypesList(){
        viewModel.transactionTypes.observe(viewLifecycleOwner) {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        }
    }

    private fun nextPageAction(string: String) {
        gotoNextPage(binding.root)
        viewModel.updateSelectedTypeTo(string)
    }
}