package ui

import models.*
import services.TiendaService
import utils.Input
import utils.Logger

class Menu(
    private val tienda: TiendaService,
    private val carrito: Carrito,
    private val impuesto: Double = 0.12
) {
    fun loop() {
        while (true) {
            try {
                println("\n===== MENÚ PRINCIPAL =====")
                println("1) Ver productos")
                println("2) Agregar producto al carrito")
                println("3) Eliminar producto del carrito")
                println("4) Ver carrito")
                println("5) Finalizar compra (Factura)")
                println("6) Salir")
                println("7) Inventario (Agregar/Editar)")

                when (Input.leerInt("Seleccione una opción: ", 1..7)) {
                    1 -> verProductos()
                    2 -> agregarProducto()
                    3 -> eliminarProducto()
                    4 -> verCarrito()
                    5 -> facturar()
                    6 -> {
                        println("👋 Gracias por usar el sistema.")
                        return
                    }
                    7 -> inventarioMenu() 
                }
            } catch (t: Throwable) {
                println("⚠️ Ocurrió un error. Revise el log.")
                Logger.error("Fallo en menú principal", t)
            }
        }
    }

    private fun verProductos() {
        println("\n📦 Productos disponibles:")
        tienda.listarProductos().forEach {
            println("${it.id}. ${it.nombre}  | Precio: ${"%.2f".format(it.precio)} | Stock: ${it.cantidadDisponible}")
        }
    }

    private fun agregarProducto() {
        val id = Input.leerInt("ID del producto: ")
        val cantidad = Input.leerInt("Cantidad: ", 1..100000)
        when (val res = tienda.agregarAlCarrito(id, cantidad)) {
            is OpResultado.Ok -> println("✅ Agregado al carrito.")
            is OpResultado.Error -> println("❌ ${res.mensaje}")
        }
    }

    private fun eliminarProducto() {
        val id = Input.leerInt("ID del producto a eliminar del carrito: ")
        when (val res = tienda.eliminarDelCarrito(id)) {
            is OpResultado.Ok -> println("🗑️ Eliminado del carrito.")
            is OpResultado.Error -> println("❌ ${res.mensaje}")
        }
    }

    private fun verCarrito() {
        if (carrito.estaVacio()) {
            println("🛒 El carrito está vacío.")
            return
        }
        println("\n🛒 Carrito:")
        carrito.obtenerItems().forEach {
            println("${it.producto.nombre} x${it.cantidad} | Unit: ${"%.2f".format(it.producto.precio)} | Subtotal: ${"%.2f".format(it.subtotal())}")
        }
        println("TOTAL: ${"%.2f".format(carrito.total())}")
    }

    private fun facturar() {
        if (carrito.estaVacio()) {
            println("🛒 No hay productos en el carrito.")
            return
        }
        if (!Input.confirmar("¿Desea confirmar la compra?")) return

        when (val res = tienda.confirmarVenta(impuesto)) {
            is VentaResultado.Fallo -> {
                println("❌ ${res.mensaje}")
            }
            is VentaResultado.Exito -> {
                val f = res.resumen
                println("\n=========== FACTURA ===========")
                f.items.forEach {
                    println("${it.producto.nombre} x${it.cantidad} | Unit: ${"%.2f".format(it.producto.precio)} | Subtotal: ${"%.2f".format(it.subtotal())}")
                }
                println("-------------------------------")
                println("Subtotal: ${"%.2f".format(f.subtotal)}")
                println("Impuesto (${(impuesto*100).toInt()}%): ${"%.2f".format(f.impuesto)}")
                println("TOTAL: ${"%.2f".format(f.total)}")
                println("===============================\n")

                try { tienda.guardarInventario() } catch (_: Throwable) { /* redundante */ }

                if (!Input.confirmar("¿Desea seguir comprando?")) {
                    println("👋 Gracias por su compra.")
                    kotlin.system.exitProcess(0)
                }
            }
        }
    }

    // -------------------- NUEVO: Submenú de Inventario --------------------

    private fun inventarioMenu() {
        while (true) {
            println("\n===== INVENTARIO =====")
            println("1) Listar productos")
            println("2) Agregar producto")
            println("3) Editar producto")
            println("4) Volver")

            when (Input.leerInt("Seleccione una opción: ", 1..4)) {
                1 -> verProductos()
                2 -> inventarioAgregar()
                3 -> inventarioEditar()
                4 -> return
            }
        }
    }

    private fun inventarioAgregar() {
        println("\n➕ Agregar nuevo producto")
        val id = Input.leerInt("ID (entero positivo): ", 1..1_000_000)
        val nombre = Input.leerString("Nombre: ")
        val precio = Input.leerDouble("Precio: ", minimo = 0.0)
        val stock = Input.leerInt("Cantidad en stock: ", 0..1_000_000)

        when (val res = tienda.agregarProductoInventario(Producto(id, nombre, precio, stock))) {
            is OpResultado.Ok -> println("✅ Producto agregado y guardado en CSV.")
            is OpResultado.Error -> println("❌ ${res.mensaje}")
        }
    }

    private fun inventarioEditar() {
        println("\n✏️ Editar producto")
        val id = Input.leerInt("ID del producto a editar: ")
        val actual = tienda.listarProductos().find { it.id == id }
        if (actual == null) {
            println("❌ No existe un producto con ID $id.")
            return
        }

        println("Actual → Nombre='${actual.nombre}', Precio=${"%.2f".format(actual.precio)}, Stock=${actual.cantidadDisponible}")
        val nuevoNombre = Input.leerString("Nuevo nombre (Enter para mantener): ", allowEmpty = true)
            .ifBlank { null } 
        val nuevoPrecio = Input.leerDoubleOpcional("Nuevo precio (Enter para mantener): ")
        val nuevoStock  = Input.leerIntOpcional("Nuevo stock (Enter para mantener): ")

        when (val res = tienda.editarProductoInventario(id, nuevoNombre, nuevoPrecio, nuevoStock)) {
            is OpResultado.Ok -> println("✅ Producto editado y guardado en CSV.")
            is OpResultado.Error -> println("❌ ${res.mensaje}")
        }
    }
}
