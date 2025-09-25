package utils

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime

enum class LogLevel { INFO, WARN, ERROR }

object Logger {
    private val archivo = File("logs/errores.log").also {
        if (!it.parentFile.exists()) it.parentFile.mkdirs()
        if (!it.exists()) it.createNewFile()
    }

    @Synchronized
    fun log(level: LogLevel, mensaje: String, t: Throwable? = null) {
        val ts = LocalDateTime.now()
        val trace = if (t != null) "\n${stackTrace(t)}" else ""
        archivo.appendText("$ts [$level] $mensaje$trace\n")
    }

    fun info(msg: String) = log(LogLevel.INFO, msg)
    fun warn(msg: String) = log(LogLevel.WARN, msg)
    fun error(msg: String, t: Throwable? = null) = log(LogLevel.ERROR, msg, t)

    private fun stackTrace(t: Throwable): String {
        val sw = StringWriter()
        t.printStackTrace(PrintWriter(sw))
        return sw.toString()
    }
}
