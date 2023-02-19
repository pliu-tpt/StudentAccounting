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
import com.example.studentaccounting.databinding.FragmentSelectCategoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SelectCategoryFragment : SelectFragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private lateinit var binding: FragmentSelectCategoryBinding
    private lateinit var adapter: OptionRecyclerViewAdapter

    private var originalMode : Int? = null

    init {
        nextPageResId = R.id.action_selectCategoryFragment_to_selectSubcategoryFragment
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
        binding = FragmentSelectCategoryBinding.inflate(inflater, container, false)

        initRecyclerView()

        viewModel.selectedCat.observe(viewLifecycleOwner) {
            binding.etNewCategory.setText(it.toString())
        }

        binding.btnCatNext.setOnClickListener {
            if (!TextUtils.isEmpty(binding.etNewCategory.text.toString())){
                nextPageAction(binding.etNewCategory.text.toString())
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please Enter a Valid Category",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        originalMode?.let { activity?.window?.setSoftInputMode(it) }
    }

//    override fun onResume() {
//        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
//        super.onResume()
//    }

    private fun initRecyclerView(){

        binding.rvCategory.layoutManager = GridLayoutManager(context, 2)
        adapter = OptionRecyclerViewAdapter{
            selectedOption: String -> nextPageAction(selectedOption)
        }
        binding.rvCategory.adapter = adapter

        displayCategoryList()
    }
    private fun displayCategoryList(){
        viewModel.categories.observe(viewLifecycleOwner) {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        }
    }

    private fun nextPageAction(string: String) {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.updateSelectedCat(string) // first update the selected cat
            gotoNextPage(binding.root) // and then create the UI
        }
    }
}