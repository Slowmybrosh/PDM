package com.example.comprapp

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PurchaseAdapter(private var groceries: MutableList<PurchaseModel>, private val action: MainFragmentAction) : RecyclerView.Adapter<PurchaseAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nameTextView = itemView.findViewById<TextView>(R.id.purchase_name)
        val priceTextView = itemView.findViewById<TextView>(R.id.purchase_price)
        //val barcodeTextView = itemView.findViewById<TextView>(R.id.purchase_barcode)
        val imageView = itemView.findViewById<ImageView>(R.id.purchase_image)
        val deleteButton = itemView.findViewById<ImageButton>(R.id.delete)
        val add_item = itemView.findViewById<ImageButton>(R.id.add_one)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val purchaseView = inflater.inflate(R.layout.item_purchase, parent, false)

        return ViewHolder(purchaseView)
    }

    override fun onBindViewHolder(viewHolder: PurchaseAdapter.ViewHolder, position: Int) {
        val purchase = groceries[position]
        val decoded64 = Base64.decode(purchase.image_base64, Base64.DEFAULT)
        viewHolder.nameTextView.text = purchase.name
        //viewHolder.barcodeTextView.text = purchase.barcode
        viewHolder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(decoded64, 0, decoded64.size))
        viewHolder.priceTextView.text = purchase.price
        viewHolder.deleteButton.setOnClickListener{
            groceries.remove(groceries[viewHolder.adapterPosition])
            notifyItemRemoved(viewHolder.adapterPosition)
        }
        viewHolder.add_item.setOnClickListener{
            groceries.add(groceries[viewHolder.adapterPosition])
            notifyItemInserted(groceries.size)
        }

        if(action == MainFragmentAction.ADD)
            viewHolder.deleteButton.visibility = View.VISIBLE
        else
            viewHolder.deleteButton.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return groceries.size
    }
}