package com.example.comprapp


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comprapp.databinding.FragmentSecondBinding

class HistoryFragment:Fragment(R.layout.fragment_second) {
    private lateinit var viewBinding: FragmentSecondBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val database = Database(context)

        viewBinding = FragmentSecondBinding.inflate(layoutInflater)
        viewBinding.historyMain.foreground.alpha = 0

        val rvHistory = viewBinding.rvHistory

        rvHistory.layoutManager = LinearLayoutManager(context)
        rvHistory.adapter = HistoryAdapter(database.getAllPurchases(), context)

        return viewBinding.root
    }
}