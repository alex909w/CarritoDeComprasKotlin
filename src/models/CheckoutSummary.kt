data class CheckoutSummary(
package models

// Resumen de la compra
data class CheckoutSummary(
    val items: List<CarritoItem>,
    val subtotal: Double,
    val impuesto: Double,
    val total: Double
)

// Resultado de la venta
sealed class VentaResultado {
    data class Exito(val resumen: CheckoutSummary): VentaResultado()
    data class Fallo(val mensaje: String): VentaResultado()
}
