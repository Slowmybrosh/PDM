package com.example.comprapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PurchaseAdapter(private var groceries: MutableList<PurchaseModel>) : RecyclerView.Adapter<PurchaseAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nameTextView = itemView.findViewById<TextView>(R.id.purchase_name)
        val priceTextView = itemView.findViewById<TextView>(R.id.purchase_price)
        val barcodeTextView = itemView.findViewById<TextView>(R.id.purchase_barcode)
        val imageView = itemView.findViewById<ImageView>(R.id.purchase_image)
        val deleteButton = itemView.findViewById<ImageButton>(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val purchaseView = inflater.inflate(R.layout.item_purchase, parent, false)

        return ViewHolder(purchaseView)
    }

    override fun onBindViewHolder(viewHolder: PurchaseAdapter.ViewHolder, position: Int) {
        val purchase = groceries[position]
        viewHolder.nameTextView.text = purchase.name
        viewHolder.barcodeTextView.text = purchase.barcode
        viewHolder.imageView.setImageBitmap(purchase.image)
        viewHolder.priceTextView.text = purchase.price
        viewHolder.deleteButton.setOnClickListener{removeItem(position); notifyItemRemoved(position)}
    }

    override fun getItemCount(): Int {
        return groceries.size
    }

    private fun removeItem(position: Int){
        groceries.removeAt(position)
    }
}