package com.example.comprapp


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class MainFragment:Fragment(R.layout.fragment_main) {
    lateinit var lastPurchase: MutableList<PurchaseModel>

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        var vista = inflater.inflate(R.layout.fragment_main, container, false)
        var rvPurchase = vista.findViewById<RecyclerView>(R.id.rvPurchase)

        //Comprobar si hay un last purchase en JSON, si lo hay. Se carga en una lista (pero esto es parte del modelo y base de datos)

        return vista
    }
}