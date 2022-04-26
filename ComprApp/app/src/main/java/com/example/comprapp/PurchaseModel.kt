package com.example.comprapp

/**
 * Clase para guardar los objetos para comprar
 *
 * @param image_base64 imagen convertida en base64 para poder ser guardada en un fichero de JSON
 * @param name nombre del item comestible
 * @param barcode código de barras del item comestible
 * @param price precio del item comestible, por defecto es "-1" e indica que es una compra futura
 */
class PurchaseModel(val image_base64: String, val name: String, val barcode: String, var quantity: Int = 0, var price: String = "0"){
}