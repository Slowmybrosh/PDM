package com.example.comprapp

import android.graphics.Bitmap

class PurchaseModel(val image: Bitmap, val name: String, val price: String, val barcode: String){

    companion object{
        private val groceries = mutableListOf<PurchaseModel>()
        fun addItem(purchase: PurchaseModel): MutableList<PurchaseModel>{
            groceries.add(purchase)
            return groceries
        }

        fun clearList(){
            groceries.clear()
        }
    }
}