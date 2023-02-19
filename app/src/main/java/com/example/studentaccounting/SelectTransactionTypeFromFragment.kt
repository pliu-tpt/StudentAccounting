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
import com.example.studentaccounting.databinding.FragmentSelectIsSpendingBinding
import com.example.studentaccounting.databinding.FragmentSelectTransactionTypeBinding
import com.example.studentaccounting.databinding.FragmentSelectTransactionTypeFromBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class SelectTransactionTypeFromFragment : SelectFragment() {
    private val viewModel : TransactionViewModel by activityViewModels()
    private lateinit var binding: FragmentSelectTransactionTypeFromBinding

    private lateinit var adapter: OptionRecyclerViewAdapter

    init {
        nextPageResId = R.id.action_selectTransactionTypeFromFragment_to_selectTransactionTypeToFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSelectTransactionTypeFromBinding.inflate(inflater, container, false)


        initRecyclerView()

        viewModel.selectedTypeFrom.observe(viewLifecycleOwner) {
            binding.etNewTypeFrom.setText(it.toString())
        }

        binding.btnTypeNextFrom.setOnClickListener {
            if (!TextUtils.isEmpty(binding.etNewTypeFrom.text.toString())){
                nextPageAction(binding.etNewTypeFrom.text.toString())
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

        binding.rvTransactionTypeFrom.layoutManager = GridLayoutManager(context, 2)
        adapter = OptionRecyclerViewAdapter{
                selectedOption: String -> nextPageAction(selectedOption)
        }
        binding.rvTransactionTypeFrom.adapter = adapter


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
        viewModel.updateSelectedTypeFrom(string)
    }
}