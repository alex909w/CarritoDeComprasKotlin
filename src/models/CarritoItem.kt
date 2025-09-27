package models

// Ítem del carrito
data class CarritoItem(
    val producto: Producto,
    var cantidad: Int
) {
    // Subtotal del ítem
    fun subtotal(): Double = producto.precio * cantidad
}
