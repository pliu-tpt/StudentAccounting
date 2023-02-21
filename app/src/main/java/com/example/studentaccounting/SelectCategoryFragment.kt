package com.example.studentaccounting

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.studentaccounting.databinding.CommonNewAddLayoutBinding
import com.example.studentaccounting.databinding.FragmentSelectCategoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SelectCategoryFragment : SelectFragment() {

    private val viewModel : TransactionViewModel by activityViewModels()
    private lateinit var adapter: OptionRecyclerViewAdapter

    private lateinit var binding: FragmentSelectCategoryBinding
    private lateinit var newAddLayoutBinding: CommonNewAddLayoutBinding

    private lateinit var fragmentString: String

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
    ): View {
        binding = FragmentSelectCategoryBinding.inflate(inflater, container, false)
        newAddLayoutBinding = CommonNewAddLayoutBinding.bind(binding.root)

        initRecyclerView()

        fragmentString = requireContext().getString(R.string.new_category)

        initNewAdd(fragmentString)

        viewModel.selectedCat.observe(viewLifecycleOwner) {
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

        return binding.root
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//        _newAddLayoutBinding = null
//    }


//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding = null
//        newAddLayoutBinding = null
//    }

    private fun initNewAdd(string: String){
        newAddLayoutBinding.etNew.hint = string
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