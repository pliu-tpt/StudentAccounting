package com.example.studentaccounting

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
import com.example.studentaccounting.databinding.FragmentSelectSubcategoryBinding
import com.example.studentaccounting.databinding.FragmentTransactionSummaryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SelectSubcategoryFragment : SelectFragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private lateinit var binding: FragmentSelectSubcategoryBinding

    private lateinit var adapter: OptionRecyclerViewAdapter

    init {
        nextPageResId = R.id.action_selectSubcategoryFragment_to_selectTransactionTypeFragment
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

        binding = FragmentSelectSubcategoryBinding.inflate(inflater, container, false)

        initRecyclerView()

        viewModel.selectedSubcat.observe(viewLifecycleOwner) {
            binding.etSubcat.setText(it.toString())
        }

        binding.btnSubcatNext.setOnClickListener {
            if (!TextUtils.isEmpty(binding.etSubcat.text.toString())){
                nextPageAction(binding.etSubcat.text.toString())
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please Enter a Valid Sub-Category",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initRecyclerView(){

        binding.rvSubcat.layoutManager = GridLayoutManager(context, 2)
        adapter = OptionRecyclerViewAdapter{
                selectedOption: String -> nextPageAction(selectedOption)
        }
        binding.rvSubcat.adapter = adapter

        displaySubcategoryList()
    }

    private fun displaySubcategoryList(){
        viewModel.subcategories.observe(viewLifecycleOwner) {
            Log.i("MYTAG",viewModel.selectedSubcategories.toString())

            viewModel.selectedSubcategories?.let { it1 -> adapter.setList(it1) }
            adapter.notifyDataSetChanged()
        }
    }

    private fun nextPageAction(string: String) {
        gotoNextPage(binding.root)
        viewModel.updateSelectedSubcat(string)
    }

}