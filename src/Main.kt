import events.*
import models.Carrito
import repository.ProductRepository
import services.TiendaService
import ui.Menu
import utils.Logger

// Punto de entrada principal
fun main() {
    val repo = ProductRepository("data/products.csv", strictLoad = true)
    Logger.info("Usando CSV en: ${repo.absolutePath()}")

    val productos = try {
        repo.load() 
    } catch (e: Exception) {
        println("❌ No se encontró el inventario en:\n   ${repo.absolutePath()}")
        println("   Crea el archivo CSV con este encabezado y datos de ejemplo:")
        println("   id,nombre,precio,cantidadDisponible")
        println("   1,Laptop,500.00,5")
        println("   2,Mouse,20.00,10")
        println("   3,Teclado,35.50,8")
        println("\nLuego vuelve a ejecutar la aplicación desde la raíz del proyecto.")
        Logger.error("No se pudo cargar inventario", e)
        return
    }

    val carrito = Carrito()
    val tienda = TiendaService(productos, carrito, repo)

    // Auditoría de eventos
    val auditor = { ev: AppEvent ->
        when (ev) {
            is ProductoAgregadoEvent -> Logger.info("EV ProductoAgregado: id=${ev.productoId}, cant=${ev.cantidad}")
            is ProductoEliminadoEvent -> Logger.info("EV ProductoEliminado: id=${ev.productoId}")
            is CheckoutEvent -> Logger.info("EV Checkout: items=${ev.items.size}, subtotal=${ev.subtotal}, impuesto=${ev.impuesto}, total=${ev.total}")
            is ErrorEvent -> Logger.error("EV Error: ${ev.mensaje}")
        }
    }
    EventBus.subscribe(auditor)

    Runtime.getRuntime().addShutdownHook(Thread {
        try { tienda.guardarInventario() } catch (_: Throwable) {}
    })

    Menu(tienda, carrito, impuesto = 0.12).loop()
}
