package com.example.comprapp


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.example.comprapp.databinding.FragmentMainBinding
import com.getbase.floatingactionbutton.FloatingActionButton

class MainFragment(private val action: String):Fragment(R.layout.fragment_main) {
    private var lastPurchase = mutableListOf<PurchaseModel>()
    private lateinit var viewBinding: FragmentMainBinding
    private lateinit var temp_barcode: String

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        viewBinding = FragmentMainBinding.inflate(layoutInflater)
        val fab_menu = viewBinding.menuFab

        val rvPurchase = viewBinding.rvPurchase
        rvPurchase.adapter = PurchaseAdapter(lastPurchase)

        if(action == "Home"){
            viewBinding.textViewRv.text = "Ultima compra"
            fab_menu.visibility = View.GONE
        }

        if(action == "Add"){
            viewBinding.textViewRv.text = "Nueva compra"
            fab_menu.visibility = View.VISIBLE

            fab_menu.findViewById<FloatingActionButton>(R.id.fab_done).setOnClickListener{
                //TODO llamar a la database para guardar la compra
                val database = DatabaseAdapter(context)
                viewBinding = database.searchBarcode("8480000141576", viewBinding)
            }
            fab_menu.findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener{
                val intent = Intent(activity, Camera::class.java)
                intent.putExtra("action", Camera_action.BARCODE)
                startActivityForResult(intent, REQUEST_BARCODE)
            }
        }


        return viewBinding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_BARCODE && resultCode == Activity.RESULT_OK){
            temp_barcode = data!!.data.toString()
            val database = DatabaseAdapter(context)
            database.searchBarcode(temp_barcode, viewBinding)

            if(viewBinding.productName.text != ""){
                val intent = Intent(activity, Camera::class.java)
                intent.putExtra("action", Camera_action.PRICE)
                startActivityForResult(intent, REQUEST_PRICE)
            }
        }
        if(requestCode == REQUEST_PRICE && resultCode == Activity.RESULT_OK){
            lastPurchase.add(PurchaseModel(viewBinding.productImage.drawable.toBitmap(),viewBinding.productName.text.toString(), data!!.getStringExtra("price").toString(),temp_barcode))
            viewBinding.rvPurchase.adapter?.notifyItemInserted(lastPurchase.size - 1 )
        }
    }

    companion object{
        val REQUEST_BARCODE = 15
        val REQUEST_PRICE = 16
    }
}