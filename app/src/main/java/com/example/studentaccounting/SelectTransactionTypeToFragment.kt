package com.example.studentaccounting

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.studentaccounting.databinding.CommonNewAddLayoutBinding
import com.example.studentaccounting.databinding.FragmentSelectTransactionTypeToBinding

class SelectTransactionTypeToFragment : SelectFragment() {
    private val viewModel : TransactionViewModel by activityViewModels()

    private lateinit var binding: FragmentSelectTransactionTypeToBinding
    private lateinit var newAddLayoutBinding: CommonNewAddLayoutBinding

    private lateinit var adapter: OptionRecyclerViewAdapter

    private lateinit var fragmentString: String

    init {
        nextPageResId = R.id.action_selectTransactionTypeToFragment_to_selectAmountFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSelectTransactionTypeToBinding.inflate(inflater, container, false)
        newAddLayoutBinding = CommonNewAddLayoutBinding.bind(binding.root)

        initRecyclerView()

        fragmentString = requireContext().getString(R.string.new_type)

        initNewAdd(fragmentString)

        viewModel.selectedTypeTo.observe(viewLifecycleOwner) {
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



//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//        _newAddLayoutBinding = null
//    }

    private fun initNewAdd(string: String){
        newAddLayoutBinding.etNew.hint = string
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