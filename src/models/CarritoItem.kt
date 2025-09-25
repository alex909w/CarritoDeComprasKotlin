package models

data class CarritoItem(
    val producto: Producto,
    var cantidad: Int
) {
    fun subtotal(): Double = producto.precio * cantidad
}
