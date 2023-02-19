package com.example.studentaccounting

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentaccounting.databinding.FragmentSelectTransactionTypeBinding
import com.example.studentaccounting.databinding.FragmentTransactionSummaryBinding


class SelectTransactionTypeFragment : SelectFragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private lateinit var binding: FragmentSelectTransactionTypeBinding

    private lateinit var adapter: OptionRecyclerViewAdapter

    init {
        nextPageResId = R.id.action_selectTransactionTypeFragment_to_selectAmountFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectTransactionTypeBinding.inflate(inflater, container, false)


        initRecyclerView()

        viewModel.selectedType.observe(viewLifecycleOwner) {
            binding.etNewType.setText(it.toString())
        }

        binding.btnTypeNext.setOnClickListener {
            if (!TextUtils.isEmpty(binding.etNewType.text.toString())){
                nextPageAction(binding.etNewType.text.toString())
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

        binding.rvTransactionType.layoutManager = GridLayoutManager(context, 2)
        adapter = OptionRecyclerViewAdapter{
                selectedOption: String -> nextPageAction(selectedOption)
        }
        binding.rvTransactionType.adapter = adapter


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
        viewModel.updateSelectedType(string)
    }

}