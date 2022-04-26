package com.example.comprapp


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comprapp.databinding.FragmentMainBinding
import com.getbase.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayOutputStream

/**
 * @author Alejandro Olivares
 *
 * Esta clase contiene el fragmento principal, que se basa en una lista de items en una compra
 *
 * @param action Especifica el contexto de la lista
 * @property lastPurchase Lista de items de la compra
 * @property mode Especifica el modo de seguimiento al añadir items
 *
* */
class MainFragment(private val action: MainFragmentAction):Fragment(R.layout.fragment_main) {
    private var lastPurchase = mutableListOf<PurchaseModel>()
    private var mode = false
    private lateinit var database: Database
    private lateinit var viewBinding: FragmentMainBinding
    private lateinit var temp_barcode: String
    private lateinit var temp_image: String
    private var lastBarcodePressed: String = ""

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        database = Database(context)
        viewBinding = FragmentMainBinding.inflate(layoutInflater)
        val fab_menu = viewBinding.menuFab
        val rvPurchase = viewBinding.rvPurchase

        if(action == MainFragmentAction.ADD){
            if(mode)
                viewBinding.textViewRv.text = "Comprando"
            else
                viewBinding.textViewRv.text = "Planificando"

            fab_menu.findViewById<FloatingActionButton>(R.id.fab_done).setOnClickListener{
                database.savePurchase(lastPurchase)
                clearList()
            }
            fab_menu.findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener{
                val intent = Intent(activity, Camera::class.java)
                intent.putExtra("action", CameraAction.BARCODE)
                startActivityForResult(intent, REQUEST_BARCODE)
            }
        }

        rvPurchase.layoutManager = LinearLayoutManager(context)
        rvPurchase.adapter = PurchaseAdapter(lastPurchase, action,object : UpdateList{
            override fun itemChanged() {
                updateTotalPrice()
            }

            override fun updatePriceCallback(barcode: String) {
                if(mode){
                    lastBarcodePressed = barcode
                    updatePrice()
                }
            }
        })

        viewBinding.changeMode.setOnClickListener {
            changeMode()
            if(mode){
                viewBinding.textViewRv.text = "Comprando"
            }
            else{
                viewBinding.textViewRv.text = "Planificando"
            }
        }
        viewBinding.changeMode.setOnLongClickListener {
            Toast.makeText(context,"Cambiar modo", Toast.LENGTH_SHORT)
            true
        }

        return viewBinding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_BARCODE && resultCode == Activity.RESULT_OK){
            temp_barcode = data!!.data.toString()
            val isOnList = checkOnList(temp_barcode)
            if(isOnList == null){
                viewBinding.productName.text = ""
                viewBinding.productImageUrl.text = ""
                database.searchBarcode(temp_barcode, viewBinding, object: VolleyCallback{
                    override fun onSuccess(action: String) {
                        super.onSuccess(action)
                        if(!mode){
                            if(viewBinding.productImageUrl.text.toString() != ""){
                                database.requestProductImage(viewBinding.productImageUrl.text.toString(), viewBinding, object: VolleyCallback{
                                    override fun onSuccess(action: String) {
                                        if(action == "Imagen"){
                                            val byteArrayOutputStream = ByteArrayOutputStream()
                                            viewBinding.productImage.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                                            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
                                            temp_image = Base64.encodeToString(byteArray, Base64.DEFAULT)

                                            lastPurchase.add(PurchaseModel(temp_image,viewBinding.productName.text.toString(),temp_barcode))

                                            viewBinding.rvPurchase.adapter?.notifyItemInserted(lastPurchase.size)
                                        }
                                    }
                                })
                            } else{
                                val d = resources.getDrawable(R.drawable.ic_default_foreground)
                                viewBinding.productImage.setImageDrawable(d)
                                val byteArrayOutputStream = ByteArrayOutputStream()
                                viewBinding.productImage.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                                val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
                                temp_image = Base64.encodeToString(byteArray, Base64.DEFAULT)

                                lastPurchase.add(PurchaseModel(temp_image,viewBinding.productName.text.toString(),temp_barcode))
                                viewBinding.rvPurchase.adapter?.notifyItemInserted(lastPurchase.size)
                            }
                        }
                    }
                })

                if(mode){
                    val intent = Intent(activity, Camera::class.java)
                    intent.putExtra("action", CameraAction.PRICE)
                    startActivityForResult(intent, REQUEST_PRICE)
                }
            } else {
                lastPurchase[isOnList].quantity++
                viewBinding.rvPurchase.adapter?.notifyItemChanged(isOnList)
                updateTotalPrice()
            }
        }
        if(requestCode == REQUEST_PRICE && resultCode == Activity.RESULT_OK){
            var array = data!!.getStringArrayListExtra("prices")
            choosePrice(array!!.toMutableList().toTypedArray(), object: ChooserCallback{
                override fun onSuccess(){
                    if(viewBinding.productImageUrl.text.toString() != ""){
                        database.requestProductImage(viewBinding.productImageUrl.text.toString(), viewBinding, object: VolleyCallback{
                            override fun onSuccess(action: String) {
                                if(action == "Imagen"){
                                    val byteArrayOutputStream = ByteArrayOutputStream()
                                    viewBinding.productImage.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                                    val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
                                    temp_image = Base64.encodeToString(byteArray, Base64.DEFAULT)

                                    lastPurchase.add(PurchaseModel(temp_image,viewBinding.productName.text.toString(),temp_barcode, 1, viewBinding.productPrice.text.toString()))
                                    viewBinding.rvPurchase.adapter?.notifyItemInserted(lastPurchase.size)
                                    updateTotalPrice()
                                }
                            }
                        })
                    } else{
                        val d = resources.getDrawable(R.drawable.ic_default_foreground)
                        viewBinding.productImage.setImageDrawable(d)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        viewBinding.productImage.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
                        temp_image = Base64.encodeToString(byteArray, Base64.DEFAULT)

                        lastPurchase.add(PurchaseModel(temp_image,viewBinding.productName.text.toString(),temp_barcode, 1, viewBinding.productPrice.text.toString()))
                        viewBinding.rvPurchase.adapter?.notifyItemInserted(lastPurchase.size)
                        updateTotalPrice()
                    }
                }
            })
        }

        if(requestCode == UPDATE_PRICE && resultCode == Activity.RESULT_OK){
            var array = data!!.getStringArrayListExtra("prices")
            choosePrice(array!!.toMutableList().toTypedArray(),object: ChooserCallback{
                override fun onSuccess() {
                    if(lastBarcodePressed != null){
                        val index = checkOnList(lastBarcodePressed)
                        if(index != null)
                            lastPurchase[index].price = viewBinding.productPrice.text.toString()
                            viewBinding.rvPurchase.adapter?.notifyItemChanged(index!!)
                            updateTotalPrice()
                    }
                }
            })
        }
    }

    /**
     * Permite elegir el precio correcto en una lista de precios detectados, o bien
     *
     * @param prices Una lista de precios detectados en String
     * @param callback Objeto de la clase ChooserCallback, al elegir el precio permite notificar la continuación de la construcción del item
     */
    private fun choosePrice(prices: Array<String>, callback: ChooserCallback){
        prices[prices.size - 1] = "No está el precio"
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Selecciona el precio")

        builder.setItems(prices) { dialog, which ->
            if(which == prices.size - 1){
                dialog.dismiss()
                manualPriceSelector(callback)
            } else{
                viewBinding.productPrice.text = prices[which]
                callback.onSuccess()
            }
        }


        val dialog = builder.create()
        dialog.show()
    }

    /**
     * En caso de que no esté el precio correcto en la lista de detectados, abre un dialogo para escribir el texto manualmente
     *
     * @param callback notifica que se ha elegido el precio
     */
    private fun manualPriceSelector(callback: ChooserCallback){
        val builder = AlertDialog.Builder(this.context)
        val dialogView = layoutInflater.inflate(R.layout.manual_chooser, null)
        builder.setTitle("Introduce el precio")
        builder.setView(dialogView)
        builder.setPositiveButton("Ok"){ dialog,_ ->
            viewBinding.productPrice.text = dialogView.findViewById<EditText>(R.id.price).text
            callback.onSuccess()
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    /**
     * Cambia el modo de seguimiento de la aplicación
     *
     * Solo hay dos modos: Seguimiento de la compra (por defecto) o Compra futura
     */
    fun changeMode(){
        this.mode =! this.mode
    }

    /**
     * Función que permite especificar un modo de seguimiento
     *
     * @param mode boolean. True para modo "comprando" y false para modo "planificando"
     */
    fun setMode(mode : Boolean){
        this.mode = mode
    }

    /**
     * Actualiza el textView con el precio de la compra actual
     */
    private fun updateTotalPrice(){
        var total = 0.0
        lastPurchase.forEach {
            total += if(it.price.contains(",")) it.price.replace(",",".").toFloat() * it.quantity else it.price.toFloat() * it.quantity
        }
        viewBinding.sumPriceNum.text = String.format("%.2f",total) + "€"
    }

    /**
     * Permite seguir el curso de una compra planificada con anterioridad, cargada desde el historial
     *
     * @param purchase lista de productos
     */
    fun setPurchase(purchase : MutableList<PurchaseModel>){
        lastPurchase = purchase
        viewBinding.rvPurchase.adapter?.notifyItemRangeInserted(0,lastPurchase.size)
    }

    /**
     * Limpia la lista de items y resetea el textView que muestra el precio
     */
    private fun clearList(){
        lastPurchase.clear()
        viewBinding.rvPurchase.adapter?.notifyItemRangeRemoved(0, lastPurchase.size)
        viewBinding.sumPriceNum.text = "0.0"
    }

    /**
     * Función que permite comprobar si un producto se encuentra en la cesta
     *
     * @param barcode código de barras del producto a comprobar en la cesta
     * @return el indice del producto en la cesta o null si no está
     */
    private fun checkOnList(barcode : String) : Int? {
        lastPurchase.forEach{
            if(it.barcode == barcode)
                return lastPurchase.indexOf(it)
        }
        return null
    }

    /**
     * Abre la cámara para actualizar el precio de un producto que se encuentra en la lista
     *
     */
    private fun updatePrice(){
        val intent = Intent(activity,Camera::class.java)
        intent.putExtra("action", CameraAction.PRICE)
        startActivityForResult(intent,UPDATE_PRICE)
    }

    /**
     * Interfaz para el Callback de peticiones HTTP
     */
    interface VolleyCallback {
        fun onSuccess(action: String){

        }
    }

    /**
     * Interfaz para el Callback para la petición de precio
     */
    interface ChooserCallback{
        fun onSuccess(){}
    }

    /**
     * Interfaz para la actualización de la lista
     */
    interface UpdateList {
        fun itemChanged(){}
        fun updatePriceCallback(barcode: String){}
    }

    companion object{
        val REQUEST_BARCODE = 15
        val REQUEST_PRICE = 16
        val UPDATE_PRICE = 17
    }
}