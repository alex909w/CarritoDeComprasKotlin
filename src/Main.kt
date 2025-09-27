package ui

import models.Carrito
import services.TiendaService
import java.util.Scanner

class Menu(
    private val tienda: TiendaService,
    private val carrito: Carrito,
    private val impuesto: Double
) {

    private val scanner = Scanner(System.`in`)

    fun loop() {
        var opcion: Int
        do {
            mostrarMenuPrincipal()
            opcion = scanner.nextInt()
            when (opcion) {
                1 -> tienda.listarProductos()
                2 -> {
                    print("Ingrese ID de producto: ")
                    val id = scanner.nextInt()
                    print("Cantidad: ")
                    val cantidad = scanner.nextInt()
                    tienda.agregarAlCarrito(id, cantidad)
                }
                3 -> {
                    print("Ingrese ID de producto a eliminar: ")
                    val id = scanner.nextInt()
                    tienda.eliminarDelCarrito(id)
                }
                4 -> tienda.checkout(impuesto)
                0 -> println("Saliendo...")
                else -> println("Opción inválida.")
            }
        } while (opcion != 0)
    }

    private fun mostrarMenuPrincipal() {
        println("\n====== Carrito de Compras ======")
        println("1. Ver inventario")
        println("2. Agregar al carrito")
        println("3. Eliminar del carrito")
        println("4. Finalizar compra")
        println("0. Salir")
        print("Seleccione una opción: ")
    }
}
