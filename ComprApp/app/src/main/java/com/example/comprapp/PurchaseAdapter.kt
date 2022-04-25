package com.example.comprapp

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

/**
 * Clase adaptador para la compra
 * gestiona la lista de compra y organiza la vista
 *
 * @param groceries lista de comestibles
 * @param action especifica el contexto de MainFragment. Puede ser HOME o ADD
 * @param callback notifica que se ha insertado o eliminado un objeto de la lista
 */
class PurchaseAdapter(private var groceries: MutableList<PurchaseModel>, private val action: MainFragmentAction, private val callback: MainFragment.UpdateList) : RecyclerView.Adapter<PurchaseAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val nameTextView = itemView.findViewById<TextView>(R.id.purchase_name)
        val priceTextView = itemView.findViewById<TextView>(R.id.purchase_price)
        val imageView = itemView.findViewById<ImageView>(R.id.purchase_image)
        val deleteButton = itemView.findViewById<ImageButton>(R.id.delete)
        val add_item = itemView.findViewById<ImageButton>(R.id.add_one)
        val quantity = itemView.findViewById<TextView>(R.id.quantity)
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
        viewHolder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(decoded64, 0, decoded64.size))
        viewHolder.quantity.text = purchase.quantity.toString()
        viewHolder.priceTextView.text = if(purchase.price != "-1") purchase.price + "€" else ""
        viewHolder.deleteButton.setOnClickListener{
            if(purchase.quantity != 1){
                purchase.quantity--
                notifyItemChanged(viewHolder.adapterPosition)
            } else {
                groceries.remove(groceries[viewHolder.adapterPosition])
                notifyItemRemoved(viewHolder.adapterPosition)
            }
            callback.itemChanged()
        }
        viewHolder.deleteButton.setOnLongClickListener {
            Toast.makeText(viewHolder.itemView.context, "Eliminar elemento", Toast.LENGTH_SHORT)
            true
        }
        viewHolder.add_item.setOnClickListener{
            purchase.quantity++
            notifyItemChanged(viewHolder.adapterPosition)
            callback.itemChanged()
        }
        viewHolder.add_item.setOnLongClickListener {
            Toast.makeText(viewHolder.itemView.context, "Duplicar elemento", Toast.LENGTH_SHORT)
            true
        }
    }

    /**
     * Obtener el número de elementos de la lista de comestibles
     *
     * @return tamaño de la lista
     */
    override fun getItemCount(): Int {
        return groceries.size
    }
}