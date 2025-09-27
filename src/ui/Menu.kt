package ui

object Menu {

    fun mostrarMenuPrincipal() {
        println("====== Carrito de Compras ======")
        println("1. Inventario")
        println("2. Carrito")
        println("3. Auditoría")
        println("0. Salir")
        print("Seleccione una opción: ")
    }

    fun mostrarSubmenuInventario() {
        println("------ Inventario ------")
        println("1. Ver productos")
        println("2. Agregar producto")
        println("3. Eliminar producto")
        println("0. Volver")
        print("Seleccione una opción: ")
    }
}
