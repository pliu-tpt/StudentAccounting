package com.example.studentaccounting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.studentaccounting.databinding.FragmentTransactionListContainerBinding

// This container class contains the nav graph for selecting a Transaction.
// Back Pressed Navigation is implemented thanks to the reimplementation of handleOnBackPressed
class TransactionListContainerFragment : Fragment() {

    private lateinit var binding: FragmentTransactionListContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTransactionListContainerBinding.inflate(inflater, container, false)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.topLayout1) as NavHostFragment

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!navHostFragment.navController.popBackStack()) {
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

        return binding.root
    }



}
