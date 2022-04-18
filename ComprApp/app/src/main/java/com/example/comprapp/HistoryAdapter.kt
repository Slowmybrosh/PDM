package com.example.comprapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.*

class HistoryAdapter(private val history: MutableList<File>, private val context : Context?) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    private lateinit var database : Database

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView = itemView.findViewById<TextView>(R.id.date_name)
        val priceTextView = itemView.findViewById<TextView>(R.id.total_price)
        val deleteButton = itemView.findViewById<ImageButton>(R.id.delete_history)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val historyView = inflater.inflate(R.layout.item_history, parent, false)
        database = Database(context)

        return ViewHolder(historyView)
    }

    @SuppressLint("CutPasteId")
    override fun onBindViewHolder(viewHolder: HistoryAdapter.ViewHolder, position: Int) {
        val purchase = history[position]

        viewHolder.itemView.setOnLongClickListener{
            val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val detailView = inflater.inflate(R.layout.popup_history, null)
            val popup = PopupWindow(detailView, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            val detailPurchase = database.getPurchase(history[viewHolder.adapterPosition])


            detailView.findViewById<RecyclerView>(R.id.rv_detail_history).layoutManager = LinearLayoutManager(context)
            detailView.findViewById<RecyclerView>(R.id.rv_detail_history).adapter = PurchaseAdapter(detailPurchase, MainFragmentAction.HOME)
            popup.isOutsideTouchable = true
            popup.showAtLocation(detailView, Gravity.CENTER, 0, 0)
            detailView.findViewById<Button>(R.id.popup_window_button).setOnClickListener{
                popup.dismiss()
            }
            true
        }
        viewHolder.dateTextView.text = parseName(purchase)
        viewHolder.priceTextView.text = String.format("%.2f",getTotalPrice(purchase))
        viewHolder.deleteButton.setOnClickListener{
            if(removeFile(history[viewHolder.adapterPosition])){
                history.remove(history[viewHolder.adapterPosition])
                notifyItemRemoved(viewHolder.adapterPosition)
            }
        }

    }

    override fun getItemCount(): Int {
        return history.size
    }

    private fun parseName(archivo: File) : String{
        return Date(archivo.lastModified()).toString()
    }

    private fun getTotalPrice(archivo: File) : Float{
        //val database = Database(context)
        val purchase = database.getPurchase(archivo)
        var total = 0.0

        purchase.forEach {
            total += if(it.price.contains(",")) it.price.replace(",",".").toFloat() else it.price.toFloat()

        }

        return total.toFloat()
    }

    private fun removeFile(archivo: File) : Boolean{
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar compra")
        builder.setMessage("¿Está seguro de que quiere eliminar la compra?")
        builder.setPositiveButton("Si"){_,_ -> database.removeFile(archivo)}
        builder.setNegativeButton("No") {_,_ ->}

        val alertDialog = builder.create()
        alertDialog.show()

        return archivo.exists()
    }
}
