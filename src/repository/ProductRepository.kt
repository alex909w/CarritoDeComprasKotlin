package repository

import models.Producto
import utils.Logger
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Modo estricto al cargar:
 * - load() SOLO lee si el CSV ya existe. NO crea el archivo.
 * - Si no existe, lanza IllegalStateException para que la app avise y termine.
 * Guardado:
 * - save() sigue creando/escribiendo el CSV (atómico + fsync) cuando hay una venta.
 */
class ProductRepository(
    csvPath: String = defaultCsvPath(),
    private val strictLoad: Boolean = true
) {

    private val path: Path = Path.of(csvPath).toAbsolutePath().normalize()

    companion object {
        private fun defaultCsvPath(): String {
            val root = System.getProperty("user.dir") // carpeta donde se ejecuta el jar
            return "$root/data/products.csv"
        }
    }

    fun absolutePath(): String = path.toString()

    fun load(): MutableList<Producto> {
        try {
            // CARGA ESTRICTA: no crear nada si no existe
            if (!Files.exists(path)) {
                val msg = "Archivo CSV no encontrado en: $path"
                Logger.error(msg)
                throw IllegalStateException(msg)
            }

            val lines = Files.readAllLines(path, StandardCharsets.UTF_8)
            if (lines.isEmpty()) return mutableListOf()

            val header = lines.first().trim()
            if (!header.equals("id,nombre,precio,cantidadDisponible", ignoreCase = true)) {
                Logger.warn("Encabezado CSV inesperado: $header")
            }

            val productos = lines.drop(1)
                .filter { it.isNotBlank() }
                .mapNotNull { parseLineToProducto(it) }
                .toMutableList()

            Logger.info("Productos cargados desde CSV: ${productos.size}")
            return productos
        } catch (t: Throwable) {
            Logger.error("Error leyendo CSV $path", t)
            throw t
        }
    }

    fun save(productos: List<Producto>) {
        ensureDirExists() // para permitir guardar tras una venta aunque no existiera antes el archivo
        // Construir contenido
        val sb = StringBuilder()
        sb.append("id,nombre,precio,cantidadDisponible\n")
        for (p in productos.sortedBy { it.id }) {
            val nombre = csvEscape(p.nombre)
            sb.append("${p.id},$nombre,${"%.2f".format(p.precio)},${p.cantidadDisponible}\n")
        }
        val bytes = sb.toString().toByteArray(StandardCharsets.UTF_8)

        // Escritura atómica + fsync
        val tmp = File.createTempFile("products_", ".tmp", path.parent.toFile()).toPath()
        try {
            FileOutputStream(tmp.toFile()).use { fos ->
                fos.write(bytes)
                fos.flush()
                fos.fd.sync() // forzar a disco
            }
            Files.move(
                tmp,
                path,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
            Logger.info("Productos guardados en CSV (${productos.size}) -> $path")
        } catch (t: Throwable) {
            Logger.error("Error guardando CSV $path", t)
            try { Files.deleteIfExists(tmp) } catch (_: Throwable) {}
            throw t
        }
    }

    // --- utilidades ---

    private fun parseLineToProducto(line: String): Producto? {
        val cells = parseCsvLine(line)
        return try {
            val id = cells.getOrNull(0)?.trim()?.toInt() ?: return null
            val nombre = cells.getOrNull(1)?.trim() ?: return null
            val precio = cells.getOrNull(2)?.trim()?.toDouble() ?: return null
            val cantidad = cells.getOrNull(3)?.trim()?.toInt() ?: return null
            Producto(id, nombre, precio, cantidad)
        } catch (t: Throwable) {
            Logger.error("Línea CSV inválida: $line", t)
            null
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"'); i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    out.add(sb.toString()); sb.setLength(0)
                }
                else -> sb.append(c)
            }
            i++
        }
        out.add(sb.toString())
        return out
    }

    private fun csvEscape(v: String): String {
        val needs = v.contains(',') || v.contains('"') || v.contains('\n') || v.contains('\r')
        return if (needs) "\"${v.replace("\"", "\"\"")}\"" else v
    }

    private fun ensureDirExists() {
        val dir = path.parent
        if (dir != null && !Files.exists(dir)) Files.createDirectories(dir)
    }
}
