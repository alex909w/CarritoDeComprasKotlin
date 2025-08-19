package models

data class Producto(
    val id: Int,
    var nombre: String,          
    var precio: Double,
    var cantidadDisponible: Int
)
