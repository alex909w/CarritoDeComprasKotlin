package services

import events.CheckoutEvent
import events.EventBus
import events.ProductoAgregadoEvent
import events.ProductoEliminadoEvent
import models.*
import repository.ProductRepository
import utils.Logger

// Servicio principal de la tienda
class TiendaService(
    private val productos: MutableList<Producto>,
    private val carrito: Carrito,
    private val repo: ProductRepository
) {
    // Lista productos ordenados
    fun listarProductos(): List<Producto> = productos.sortedBy { it.id }

    // Agrega producto al carrito
    fun agregarAlCarrito(id: Int, cantidad: Int): OpResultado {
        val p = productos.find { it.id == id } ?: return OpResultado.Error("Producto no encontrado.")
        if (cantidad <= 0) return OpResultado.Error("La cantidad debe ser > 0.")

        val yaEnCarrito = carrito.obtenerItems().find { it.producto.id == id }?.cantidad ?: 0
        val disponibleEfectivo = p.cantidadDisponible - yaEnCarrito
        if (cantidad > disponibleEfectivo) return OpResultado.Error("Stock insuficiente. Disponible: $disponibleEfectivo")

        val res = carrito.agregar(p, cantidad)
        if (res is OpResultado.Ok) {
            EventBus.publish(ProductoAgregadoEvent(p.id, cantidad))
            Logger.info("Agregado al carrito (sin afectar stock): id=${p.id}, cant=$cantidad, enCarrito=${yaEnCarrito + cantidad}, stockDB=${p.cantidadDisponible}")
        }
        return res
    }

    // Elimina producto del carrito
    fun eliminarDelCarrito(id: Int): OpResultado {
        val item = carrito.obtenerItems().find { it.producto.id == id }
            ?: return OpResultado.Error("El producto no está en el carrito.")
        val res = carrito.eliminar(id)
        if (res is OpResultado.Ok) {
            EventBus.publish(ProductoEliminadoEvent(id))
            Logger.info("Eliminado del carrito (sin afectar stock): id=$id, cantEliminada=${item.cantidad}")
        }
        return res
    }

    // Confirma venta y descuenta stock
    fun confirmarVenta(impuesto: Double): VentaResultado {
        if (carrito.estaVacio()) return VentaResultado.Fallo("El carrito está vacío.")

        val items = carrito.obtenerItems().map { it.copy() }
        for (it in items) {
            if (it.cantidad > it.producto.cantidadDisponible) {
                return VentaResultado.Fallo("Stock insuficiente para '${it.producto.nombre}'. Disponible: ${it.producto.cantidadDisponible}")
            }
        }

        val subtotal = items.sumOf { it.subtotal() }
        val imp = subtotal * impuesto
        val total = subtotal + imp

        items.forEach { it.producto.cantidadDisponible -= it.cantidad }
        try {
            repo.save(productos)
        } catch (t: Throwable) {
            Logger.error("No se pudo guardar el CSV durante la venta.", t)
            return VentaResultado.Fallo("No se pudo guardar el inventario. Venta cancelada.")
        }

        EventBus.publish(CheckoutEvent(items, subtotal, imp, total))
        Logger.info("Venta confirmada. items=${items.size}, subtotal=$subtotal, imp=$imp, total=$total")
        carrito.vaciar()

        return VentaResultado.Exito(CheckoutSummary(items, subtotal, imp, total))
    }

    fun guardarInventario() = repo.save(productos)

    // Inventario: agregar producto
    fun agregarProductoInventario(nuevo: Producto): OpResultado {
        if (productos.any { it.id == nuevo.id }) return OpResultado.Error("Ya existe un producto con el ID ${nuevo.id}.")
        if (nuevo.nombre.isBlank()) return OpResultado.Error("El nombre no puede estar vacío.")
        if (nuevo.precio < 0.0) return OpResultado.Error("El precio no puede ser negativo.")
        if (nuevo.cantidadDisponible < 0) return OpResultado.Error("La cantidad no puede ser negativa.")

        productos.add(nuevo)
        return try {
            repo.save(productos)
            Logger.info("Inventario: agregado id=${nuevo.id}, nombre='${nuevo.nombre}', precio=${nuevo.precio}, stock=${nuevo.cantidadDisponible}")
            OpResultado.Ok
        } catch (t: Throwable) {
            Logger.error("Inventario: error guardando CSV al agregar.", t)
            OpResultado.Error("No se pudo guardar el CSV.")
        }
    }

    // Inventario: editar producto
    fun editarProductoInventario(
        id: Int,
        nombre: String?,
        precio: Double?,
        cantidad: Int?
    ): OpResultado {
        val p = productos.find { it.id == id } ?: return OpResultado.Error("No existe un producto con ID $id.")

        if (nombre != null) {
            if (nombre.isBlank()) return OpResultado.Error("El nombre no puede estar vacío.")
            p.nombre = nombre
        }
        if (precio != null) {
            if (precio < 0.0) return OpResultado.Error("El precio no puede ser negativo.")
            p.precio = precio
        }
        if (cantidad != null) {
            if (cantidad < 0) return OpResultado.Error("La cantidad no puede ser negativa.")
            p.cantidadDisponible = cantidad
        }

        return try {
            repo.save(productos)
            Logger.info("Inventario: editado id=$id, nombre='${p.nombre}', precio=${p.precio}, stock=${p.cantidadDisponible}")
            OpResultado.Ok
        } catch (t: Throwable) {
            Logger.error("Inventario: error guardando CSV al editar.", t)
            OpResultado.Error("No se pudo guardar el CSV.")
        }
    }
}
