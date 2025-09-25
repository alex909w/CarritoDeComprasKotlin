package utils

object Input {

    fun leerInt(mensaje: String, rango: IntRange? = null): Int {
        while (true) {
            print(mensaje)
            val txt = readlnOrNull()?.trim()
            val valor = txt?.toIntOrNull()
            if (valor == null) {
                println("❌ Ingrese un número entero válido.")
                continue
            }
            if (rango != null && valor !in rango) {
                println("❌ Debe estar en el rango $rango.")
                continue
            }
            return valor
        }
    }

    fun leerDouble(mensaje: String, minimo: Double? = null): Double {
        while (true) {
            print(mensaje)
            val txt = readlnOrNull()?.trim()
            val valor = txt?.toDoubleOrNull()
            if (valor == null) {
                println("❌ Ingrese un número válido (decimal con punto si aplica).")
                continue
            }
            if (minimo != null && valor < minimo) {
                println("❌ Debe ser ≥ $minimo.")
                continue
            }
            return valor
        }
    }

    fun leerString(mensaje: String, allowEmpty: Boolean = false): String {
        while (true) {
            print(mensaje)
            val txt = readlnOrNull()?.trim() ?: ""
            if (!allowEmpty && txt.isBlank()) {
                println("❌ No puede estar vacío.")
                continue
            }
            return txt
        }
    }

    // Para edición: si el usuario presiona Enter, retorna null (no cambiar)
    fun leerIntOpcional(mensaje: String): Int? {
        print(mensaje)
        val txt = readlnOrNull()?.trim().orEmpty()
        if (txt.isBlank()) return null
        val v = txt.toIntOrNull()
        if (v == null) {
            println("❌ Número inválido, se mantiene el valor actual.")
            return null
        }
        return v
    }

    fun leerDoubleOpcional(mensaje: String): Double? {
        print(mensaje)
        val txt = readlnOrNull()?.trim().orEmpty()
        if (txt.isBlank()) return null
        val v = txt.toDoubleOrNull()
        if (v == null) {
            println("❌ Número inválido, se mantiene el valor actual.")
            return null
        }
        return v
    }

    fun confirmar(mensaje: String): Boolean {
        while (true) {
            print("$mensaje (s/n): ")
            when (readlnOrNull()?.trim()?.lowercase()) {
                "s", "si", "sí" -> return true
                "n", "no" -> return false
                else -> println("❌ Responda 's' o 'n'.")
            }
        }
    }
}
