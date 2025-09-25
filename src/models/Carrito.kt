package models

sealed class OpResultado {
    data object Ok : OpResultado()
    data class Error(val mensaje: String) : OpResultado()
}

class Carrito {
    private val items = mutableListOf<CarritoItem>()

    fun agregar(producto: Producto, cantidad: Int): OpResultado {
        if (cantidad <= 0) return OpResultado.Error("La cantidad debe ser mayor a 0.")
        val existente = items.find { it.producto.id == producto.id }
        if (existente != null) {
            existente.cantidad += cantidad
        } else {
            items.add(CarritoItem(producto, cantidad))
        }
        return OpResultado.Ok
    }

    fun eliminar(idProducto: Int): OpResultado {
        val removed = items.removeIf { it.producto.id == idProducto }
        return if (removed) OpResultado.Ok else OpResultado.Error("El producto no está en el carrito.")
    }

    fun total(): Double = items.sumOf { it.subtotal() }
    fun estaVacio(): Boolean = items.isEmpty()
    fun obtenerItems(): List<CarritoItem> = items.toList()
    fun vaciar() = items.clear()
}
