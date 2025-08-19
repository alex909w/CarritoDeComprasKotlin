package models

data class CheckoutSummary(
    val items: List<CarritoItem>,
    val subtotal: Double,
    val impuesto: Double,
    val total: Double
)

sealed class VentaResultado {
    data class Exito(val resumen: CheckoutSummary): VentaResultado()
    data class Fallo(val mensaje: String): VentaResultado()
}
