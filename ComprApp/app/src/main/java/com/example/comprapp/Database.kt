package com.example.comprapp

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.comprapp.databinding.FragmentMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Clase que actua como base de datos
 *
 * @param context Contexto de la aplicación
 * @property requestQueue Cola para peticiones HTTP
 */

class Database(private val context: Context?) {
    private var requestQueue = Volley.newRequestQueue(context)

    /**
     * Buscar el código de barras en la API de OpenFoods
     *
     * @param barcode código de barras detectado por la cámara
     * @param viewBinding contiene la vista para acceder rápidamente a los elementos de la misma
     * @param callback notificar que se ha obtenido una respuesta de la base de datos
     */
    fun searchBarcode(barcode: String, viewBinding: FragmentMainBinding, callback: MainFragment.VolleyCallback){
        val regex = "^[0-9]{13}$".toRegex()
        var url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
        if (regex.matches(barcode)) {
            val request = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    try {
                        val json_array = response.getJSONObject("product")

                        viewBinding.productName.text = json_array.getString("product_name")
                        viewBinding.productImageUrl.text = json_array.getString("image_front_small_url").orEmpty()
                        Log.d("ComprApp", "Se ha recibido la respuesta del código de barras")
                        callback.onSuccess("barcode")

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }) { e -> e.printStackTrace() }
            requestQueue.add(request)
        }
    }

    /**
     * Guardar compra en el almacenamiento interno
     *
     * @param groceries lista con todos los item de una compra
     */
    fun savePurchase(groceries: MutableList<PurchaseModel>) {
        val jsonString = Gson().toJson(groceries)
        val file = File(context?.filesDir, DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
        file.writeText(jsonString)
    }

    /**
     * Obtener compra desde un fichero
     *
     * @param file archivo JSON que contiene una lista de items
     * @return lista de items para comprar
     */
    fun getPurchase(file: File): MutableList<PurchaseModel> {
        val mutableListType = object : TypeToken<MutableList<PurchaseModel>>() {}.type
        return Gson().fromJson(file.readText(), mutableListType)
    }

    /**
     * Obtener la última compra basada en la fecha de la compra
     *
     * @return la lista de la última compra, si no hay última compra se devuelve null
     */

    fun getLastPurchase(): MutableList<PurchaseModel>? {
        val dir = File(context?.filesDir!!.path)
        var lastModified: Long = -1
        var path = ""
        if (dir.walk().iterator().hasNext() && dir.walk().toList().last().isFile) {
            val lastFile = dir.walk().toList().last()
            val mutableListType = object : TypeToken<MutableList<PurchaseModel>>() {}.type
            return Gson().fromJson(lastFile.readText(), mutableListType)
        }
        return null

    }

    /**
     * Obtener todas las compras
     *
     * @return devuelve una lista de ficheros JSON, cada uno con una compra
     */
    fun getAllPurchases() : MutableList<File>{
        var list = File(context?.filesDir!!.path).walk().toMutableList()
        list.removeAt(0)
        return list
    }

    /**
     * Eliminar archivo
     *
     * @param archivo fichero a eliminar
     */
    fun removeFile(archivo: File) {
        archivo.delete()
    }

    /**
     * Solicitar imagen del comestible a la base de datos
     *
     * @param url link de la imagen
     * @param viewBinding contiene la vista para acceder rápidamente a los elementos de la misma
     * @param callback notifica la recepción de la respuesta HTTP
     */
    fun requestProductImage(url: String, viewBinding: FragmentMainBinding, callback: MainFragment.VolleyCallback){
        if(url != ""){
            val imageRequest = ImageRequest(url,{
                bitmap ->
                viewBinding.productImage.setImageBitmap(bitmap)
                callback.onSuccess("Imagen")
            }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888, {
                error -> error.printStackTrace()
            })
            requestQueue.add(imageRequest)
        }
    }

}