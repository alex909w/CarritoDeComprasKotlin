package models

class Factura(
    private val items: List<CarritoItem>,
    private val impuesto: Double = 0.12
) {
    fun generarFactura() {
        println("\n=========== FACTURA ===========")
        for (item in items) {
            println(
                "${item.producto.nombre} x${item.cantidad} " +
                "Precio Unit: ${"%.2f".format(item.producto.precio)} " +
                "Subtotal: ${"%.2f".format(item.subtotal())}"
            )
        }
        // Evita ambigüedad usando fold con Double
        val subtotal = items.fold(0.0) { acc, it -> acc + it.subtotal() }
        val impuestos = subtotal * impuesto
        val total = subtotal + impuestos
        println("-------------------------------")
        println("Subtotal: ${"%.2f".format(subtotal)}")
        println("Impuestos: ${"%.2f".format(impuestos)}")
        println("TOTAL A PAGAR: ${"%.2f".format(total)}")
        println("===============================\n")
    }
}
