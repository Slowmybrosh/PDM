package com.example.comprapp


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comprapp.databinding.FragmentMainBinding
import com.getbase.floatingactionbutton.FloatingActionButton


class MainFragment(private val action: String):Fragment(R.layout.fragment_main) {
    private var lastPurchase = mutableListOf<PurchaseModel>()
    private lateinit var viewBinding: FragmentMainBinding
    private lateinit var temp_barcode: String
    private lateinit var temp_name: String
    private lateinit var temp_image: String

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        viewBinding = FragmentMainBinding.inflate(layoutInflater)
        val fab_menu = viewBinding.menuFab

        val rvPurchase = viewBinding.rvPurchase

        if(action == "Home"){ //TODO Cambiar lo de Action por un enum
            viewBinding.textViewRv.text = "Ultima compra"
            fab_menu.visibility = View.GONE
            val database = DatabaseAdapter(context)
            if(database.getLastPurchase() != null) lastPurchase = database.getLastPurchase()!!

        }

        if(action == "Add"){
            viewBinding.textViewRv.text = "Nueva compra"
            fab_menu.visibility = View.VISIBLE

            fab_menu.findViewById<FloatingActionButton>(R.id.fab_done).setOnClickListener{
                val database = DatabaseAdapter(context)
                //database.savePurchase(lastPurchase)
                //database.searchBarcode("8480000160447", viewBinding)
                //choosePrice()
            }
            fab_menu.findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener{
                val intent = Intent(activity, Camera::class.java)
                intent.putExtra("action", Camera_action.BARCODE)
                startActivityForResult(intent, REQUEST_BARCODE)
            }
        }

        rvPurchase.layoutManager = LinearLayoutManager(context)
        rvPurchase.adapter = PurchaseAdapter(lastPurchase, action)

        return viewBinding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_BARCODE && resultCode == Activity.RESULT_OK){
            temp_barcode = data!!.data.toString()
            val database = DatabaseAdapter(context)
            database.searchBarcode(temp_barcode, viewBinding)

            val intent = Intent(activity, Camera::class.java)
            intent.putExtra("action", Camera_action.PRICE)
            startActivityForResult(intent, REQUEST_PRICE)
        }
        if(requestCode == REQUEST_PRICE && resultCode == Activity.RESULT_OK){
            var array = data!!.getStringArrayListExtra("prices")
            choosePrice(array!!.toMutableList().toTypedArray())
            //val byteArrayOutputStream = ByteArrayOutputStream()
            //viewBinding.productImage.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            //val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            //temp_image = Base64.encodeToString(byteArray, Base64.DEFAULT)
            //
            //lastPurchase.add(PurchaseModel(temp_image,temp_name, data!!.getStringExtra("price").toString(),temp_barcode))
            //viewBinding.rvPurchase.adapter?.notifyItemInserted(lastPurchase.size)
        }
    }

    private fun choosePrice(prices: Array<String>){
        prices[prices.size - 1] = "No está el precio"
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Selecciona el precio")

        builder.setItems(prices) { dialog, which ->
            viewBinding.productPrice.text = prices[which]
        }


        val dialog = builder.create()
        dialog.show()
    }

    private fun manualPriceSelector(){

    }

    companion object{
        val REQUEST_BARCODE = 15
        val REQUEST_PRICE = 16
    }
}