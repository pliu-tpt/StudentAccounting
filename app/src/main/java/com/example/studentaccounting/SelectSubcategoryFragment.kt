package com.example.studentaccounting

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.studentaccounting.databinding.CommonNewAddLayoutBinding
import com.example.studentaccounting.databinding.FragmentSelectSubcategoryBinding


class SelectSubcategoryFragment : SelectFragment() {

    private val viewModel : TransactionViewModel by activityViewModels()

    private lateinit var binding: FragmentSelectSubcategoryBinding
    private lateinit var newAddLayoutBinding: CommonNewAddLayoutBinding

    private lateinit var adapter: OptionRecyclerViewAdapter

    private lateinit var fragmentString: String

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
    ): View {

        binding = FragmentSelectSubcategoryBinding.inflate(inflater, container, false)
        newAddLayoutBinding = CommonNewAddLayoutBinding.bind(binding.root)

        initRecyclerView()

        fragmentString = requireContext().getString(R.string.new_sub_category)

        initNewAdd(fragmentString)

        viewModel.selectedSubcat.observe(viewLifecycleOwner) {
            newAddLayoutBinding.etNew.setText(it.toString())
        }

        newAddLayoutBinding.btnNewNext.setOnClickListener {
            if (!TextUtils.isEmpty(newAddLayoutBinding.etNew.text.toString())){
                nextPageAction(newAddLayoutBinding.etNew.text.toString())
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please Enter a Valid $fragmentString",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//        _newAddLayoutBinding = null
//    }

    private fun initNewAdd(string: String){
        newAddLayoutBinding.etNew.hint = string
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