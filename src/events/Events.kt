package events

import models.CarritoItem

sealed interface AppEvent

data class ProductoAgregadoEvent(val productoId: Int, val cantidad: Int) : AppEvent
data class ProductoEliminadoEvent(val productoId: Int) : AppEvent
data class CheckoutEvent(
    val items: List<CarritoItem>,
    val subtotal: Double,
    val impuesto: Double,
    val total: Double
) : AppEvent
data class ErrorEvent(val mensaje: String) : AppEvent
